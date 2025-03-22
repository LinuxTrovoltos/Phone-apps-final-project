package com.example.aroundme.data.repository

import android.net.Uri
import com.example.aroundme.database.PostDao
import com.example.aroundme.database.UserDao
import com.example.aroundme.data.repository.firebase.FirebasePostDataSource
import com.example.aroundme.database.PostEntity
import com.example.aroundme.database.PostWithUser
import com.example.aroundme.domain.repository.PostRepository
import com.example.aroundme.models.Post
import com.example.aroundme.utils.toDomain
import com.example.aroundme.utils.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val userDao: UserDao,
    private val firebase: FirebasePostDataSource
) : PostRepository {

    override fun getPosts(): Flow<List<Post>> =
        postDao.getAllPosts().map { list -> list.map { it.toDomain() } }

    override fun getPostsWithUser(): Flow<List<PostWithUser>> =
        postDao.getAllPosts().map { posts ->
            posts.map { post ->
                val user = userDao.getUser(post.creatorId)
                PostWithUser(post.toDomain(), user)
            }
        }


    override suspend fun createPost(post: Post, imageUri: Uri?): Result<Unit> {
        return try {
            val imageUrl = imageUri?.let {
                firebase.uploadImage(it, post.id) // may throw
            }

            val finalPost = post.copy(imageUrl = imageUrl)

            // Always cache locally first
            postDao.upsert(finalPost.toEntity().copy(isSynced = false))

            // Try to upload to Firestore
            val success = firebase.uploadPost(finalPost)

            if (success) {
                postDao.markSynced(post.id)
                Result.success(Unit)
            }
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun deletePost(postId: String) {
        firebase.deletePost(postId)
        postDao.deleteById(postId)
    }

    override suspend fun fetchRemotePosts() {
        firebase.getPosts().collect { remotePosts ->
            postDao.insertAll(remotePosts.map { it.toEntity() })
        }
    }

    override suspend fun syncPosts() {
        val unsynced = postDao.getUnsyncedPosts()
        unsynced.forEach {
            val post = it.toDomain()
            if (firebase.uploadPost(post)) {
                postDao.markSynced(post.id)
            }
        }
    }
}
