package com.assignment.imagestreaming.services

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.assignment.imagestreaming.data.NetworkResult
import com.assignment.imagestreaming.data.service.FileUploadService
import com.assignment.imagestreaming.utils.AppUtils.compressImage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.File

class ImageUploadWorker(
    val context: Context,
    workerParams: WorkerParameters,
    private val uploadService: FileUploadService

) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            Log.d(
                "ImageUploadWorker",
                "Starting work with URI: ${inputData.getString(KEY_IMAGE_URI)}"
            )

            val imageUri = inputData.getString(KEY_IMAGE_URI)
            if (imageUri != null) {
                val file = File(Uri.parse(imageUri).path!!)
                val compressedFile = compressImage(file, 80)

                val requestFile = compressedFile.asRequestBody("image/*".toMediaTypeOrNull())
                val multipartBody =
                    MultipartBody.Part.createFormData("file", compressedFile.name, requestFile)

                val response = uploadService.uploadFile(multipartBody)


                if (response.isSuccessful) {
                    Result.success()
                } else {
                    Log.e("ImageUploadWorker", "Upload failed with code: ${response.code()}")
                    Result.retry()
                }
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e("ImageUploadWorker", "Error uploading image", e)
            Result.retry()
        }
    }

    companion object {
        const val KEY_IMAGE_URI = "key_image_uri"
    }
}