package com.example.aroundme.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.aroundme.domain.repository.PostRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PostSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val postRepository: PostRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            postRepository.syncPosts()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
