package com.assignment.imagestreaming.services

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.assignment.imagestreaming.data.service.FileUploadService
import javax.inject.Inject

class ImageUploadWorkerFactory @Inject constructor(
    private var uploadService: FileUploadService
) : WorkerFactory(){
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when(workerClassName){
            ImageUploadWorker::class.java.name ->
                ImageUploadWorker(appContext, workerParameters, uploadService)
            else -> null
        }
    }

}