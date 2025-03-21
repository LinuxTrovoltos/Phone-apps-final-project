package com.example.aroundme.data.repository

import com.example.aroundme.database.PostDao
import com.example.aroundme.domain.repository.PostRepository
import com.example.aroundme.models.Post
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class PostRepositoryImpl(
    private val dao: PostDao,
    private val firestore: FirebaseFirestore
) : PostRepository {

    override fun getAllPosts(): Flow<List<Post>> {
        return dao.getLivePosts()
    }

    override suspend fun addOrUpdatePost(post: Post) {
        dao.insertPost(post.copy(isSynced = false))
    }

    override suspend fun syncPendingPosts() = withContext(Dispatchers.IO) {
        val unsyncedPosts = dao.getUnsyncedPosts()

        for (post in unsyncedPosts) {
            try {
                firestore.collection("posts")
                    .document(post.id)
                    .set(post)
                    .await()  // ✅ suspends instead of using callbacks

                dao.insertPost(post.copy(isSynced = true))

            } catch (e: Exception) {
                e.printStackTrace()
                // You could log or mark the post as failed if needed
            }
        }
    }
}
