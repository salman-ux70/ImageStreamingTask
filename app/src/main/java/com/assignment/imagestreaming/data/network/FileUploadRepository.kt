package com.assignment.imagestreaming.data.network

import com.assignment.imagestreaming.data.NetworkResult
import com.assignment.imagestreaming.model.ImageUploadingResponse
import okhttp3.MultipartBody

interface FileUploadRepository {
    suspend fun uploadFile(file: MultipartBody.Part): NetworkResult<ImageUploadingResponse>
}