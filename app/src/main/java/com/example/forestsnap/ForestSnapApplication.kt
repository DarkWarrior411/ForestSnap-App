package com.example.forestsnap

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Custom Application class initializing Hilt dependency injection and WorkManager configuration.
 */
@HiltAndroidApp
class ForestSnapApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // Custom WorkManager configuration providing Hilt-injected worker instances
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
