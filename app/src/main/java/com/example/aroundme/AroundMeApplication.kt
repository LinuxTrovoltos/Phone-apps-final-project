package com.example.aroundme

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.example.aroundme.workers.PostSyncWorker
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class AroundMeApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleSyncWorker()
        if (!Places.isInitialized()) {
            Places.initialize(this, getString(R.string.GOOGLE_MAPS_API_KEY))
        }
    }

    private fun scheduleSyncWorker() {
        val workRequest = PeriodicWorkRequestBuilder<PostSyncWorker>(
            1, TimeUnit.MINUTES
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "PostSyncJob",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
