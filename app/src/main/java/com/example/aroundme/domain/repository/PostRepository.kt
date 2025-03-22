package com.example.aroundme.domain.repository

import com.example.aroundme.models.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun getPosts(): Flow<List<Post>>
    suspend fun createPost(post: Post)
    suspend fun syncPosts()
    suspend fun fetchRemotePosts()
}
