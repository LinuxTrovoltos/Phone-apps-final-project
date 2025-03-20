package com.example.aroundme.utils

import android.content.Context
import androidx.work.*
import com.example.aroundme.workers.SyncWorker
import java.util.concurrent.TimeUnit

object SyncHelper {

    fun triggerSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED) // Sync only if online
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(syncRequest)
    }
}
