package com.assignment.imagestreaming.app

import android.app.Application
import androidx.work.Configuration
import com.assignment.imagestreaming.services.ImageUploadWorkerFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ImageApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: ImageUploadWorkerFactory

    override fun getWorkManagerConfiguration() = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()
}