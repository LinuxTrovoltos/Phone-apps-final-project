package com.example.aroundme.data.repository

import com.example.aroundme.database.PostDao
import com.example.aroundme.utils.toDomain
import com.example.aroundme.utils.toEntity
import com.example.aroundme.domain.repository.PostRepository
import com.example.aroundme.models.Post
import com.example.aroundme.data.repository.firebase.FirebasePostDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val firebase: FirebasePostDataSource
) : PostRepository {

    override fun getPosts(): Flow<List<Post>> = dao.getAllPosts()
        .map { list -> list.map { it.toDomain() } }

    override suspend fun createPost(post: Post) {
        dao.upsert(post.toEntity().copy(isSynced = false))
    }

    override suspend fun syncPosts() {
        val unsynced = dao.getUnsyncedPosts()
        unsynced.forEach {
            val post = it.toDomain()
            if (firebase.uploadPost(post)) {
                dao.markSynced(post.id)
            }
        }
    }

    override suspend fun fetchRemotePosts() {
        firebase.getPosts().collect { remote ->
            dao.insertAll(remote.map { it.toEntity() })
        }
    }
}
