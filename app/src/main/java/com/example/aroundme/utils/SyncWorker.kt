package com.example.aroundme.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.aroundme.database.PostDatabase
import com.example.aroundme.models.Post
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val db = FirebaseFirestore.getInstance()
    private val postDatabase = PostDatabase.getDatabase(context)

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val offlinePosts = postDatabase.postDao().getAllPosts()
                if (offlinePosts.isNotEmpty()) {
                    for (post in offlinePosts) {
                        db.collection("posts").add(post)
                            .addOnSuccessListener {
                                Log.d("SyncWorker", "Offline post synced: ${post.title}")
                                withContext(Dispatchers.IO) {
                                    postDatabase.postDao().deleteAllPosts()
                                }
                            }
                            .addOnFailureListener {
                                Log.e("SyncWorker", "Failed to sync post: ${post.title}")
                            }
                    }
                }
                Result.success()
            } catch (e: Exception) {
                Log.e("SyncWorker", "Error syncing posts", e)
                Result.retry()
            }
        }
    }
}
