package com.assignment.imagestreaming.data.service

import com.assignment.imagestreaming.data.NetworkResult
import com.assignment.imagestreaming.model.ImageUploadingResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FileUploadService {
    @Multipart
    @POST("upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): Response<ImageUploadingResponse>
}