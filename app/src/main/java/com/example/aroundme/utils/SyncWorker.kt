package com.example.aroundme.utils

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.aroundme.database.PostDatabase
import com.example.aroundme.models.Post
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val db = FirebaseFirestore.getInstance()
    private val postDatabase = PostDatabase.getDatabase(context)

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val offlinePosts = postDatabase.postDao().getUnsyncedPosts()

                for (post in offlinePosts) {
                    try {
                        db.collection("posts").document(post.id).set(post).await()

                        // Mark as synced in Room
                        postDatabase.postDao().insertPost(post.copy(isSynced = true))

                        Log.d("SyncWorker", "Synced: ${post.title}")
                    } catch (e: Exception) {
                        Log.e("SyncWorker", "Failed to sync post: ${post.title}", e)
                        // Optional: add retry logic or error flag in DB
                    }
                }

                Result.success()
            } catch (e: Exception) {
                Log.e("SyncWorker", "Unexpected error", e)
                Result.retry()
            }
        }
    }
}
