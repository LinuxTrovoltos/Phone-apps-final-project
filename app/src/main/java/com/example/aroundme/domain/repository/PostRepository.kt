package com.example.aroundme.domain.repository

import com.example.aroundme.models.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun getAllPosts(): Flow<List<Post>>
    suspend fun addOrUpdatePost(post: Post)
    suspend fun syncPendingPosts()
}
