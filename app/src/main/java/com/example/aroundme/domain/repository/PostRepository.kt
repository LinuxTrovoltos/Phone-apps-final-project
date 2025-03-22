package com.example.aroundme.domain.repository

import android.net.Uri
import com.example.aroundme.database.PostWithUser
import com.example.aroundme.models.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun getPosts(): Flow<List<Post>>
    fun getPostsWithUser(): Flow<List<PostWithUser>>
    suspend fun createPost(post: Post, imageUri: Uri?): Result<Unit>
    suspend fun deletePost(postId: String)
    suspend fun syncPosts()
    suspend fun fetchRemotePosts()
}
