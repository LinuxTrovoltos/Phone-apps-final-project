package com.example.aroundme.utils

import android.content.Context
import androidx.work.*
import com.example.aroundme.workers.PostSyncWorker
import java.util.concurrent.TimeUnit

object SyncHelper {
    fun triggerSync(context: Context) {
        val request = OneTimeWorkRequestBuilder<PostSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}
