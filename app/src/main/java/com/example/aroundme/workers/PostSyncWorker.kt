package com.example.aroundme.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.aroundme.domain.repository.PostRepository

class PostSyncWorker(
    context: Context,
    params: WorkerParameters,
    private val repository: PostRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            repository.syncPendingPosts()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
