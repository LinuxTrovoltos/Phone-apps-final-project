package com.example.aroundme.database

import androidx.room.*
import com.example.aroundme.models.Post
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post)

    @Query("SELECT * FROM posts ORDER BY updatedAt DESC")
    suspend fun getAllPosts(): List<Post>

    @Query("SELECT * FROM posts ORDER BY updatedAt DESC")
    fun getLivePosts(): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE isSynced = 0")
    suspend fun getUnsyncedPosts(): List<Post>

    @Query("DELETE FROM posts")
    suspend fun deleteAllPosts()

    @Query("SELECT * FROM posts WHERE id = :postId")
    suspend fun getPostById(postId: String): Post?

    @Query("SELECT * FROM posts WHERE title LIKE '%' || :query || '%' OR category = :category")
    suspend fun searchPosts(query: String, category: String): List<Post>
}
