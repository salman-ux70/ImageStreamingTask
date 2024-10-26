package com.assignment.imagestreaming.data

import com.assignment.imagestreaming.model.ImageUploadingResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response

interface FileUploadRepository {
    suspend fun uploadFile(file: MultipartBody.Part): NetworkResult<ImageUploadingResponse>
}