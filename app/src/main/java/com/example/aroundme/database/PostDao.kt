package com.example.aroundme.database
//
//import androidx.room.Dao
//import androidx.room.Insert
//import androidx.room.OnConflictStrategy
//import androidx.room.Query
//import com.example.aroundme.models.Post
//
//@Dao
//interface PostDao {
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertPost(post: Post)
//
//    @Query("SELECT * FROM posts")
//    suspend fun getAllPosts(): List<Post>
//
//    @Query("DELETE FROM posts")
//    suspend fun deleteAllPosts()
//
//    @Query("SELECT * FROM posts WHERE title LIKE '%' || :query || '%' OR category = :category")
//    suspend fun searchPosts(query: String, category: String): List<Post>
//
//}
