package com.assignment.imagestreaming.data.network

import com.assignment.imagestreaming.data.NetworkResult
import com.assignment.imagestreaming.data.service.FileUploadService
import com.assignment.imagestreaming.model.ImageUploadingResponse
import okhttp3.MultipartBody
import javax.inject.Inject

class FileUploadRepositoryImpl @Inject constructor(
    private val fileUploadService: FileUploadService
) : FileUploadRepository {
    override suspend fun uploadFile(file: MultipartBody.Part): NetworkResult<ImageUploadingResponse> {
        return try {
            val response = fileUploadService.uploadFile(file)
            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    NetworkResult.Success(responseBody)
                } ?: NetworkResult.Error("Empty response body", response.code())
            } else {
                val errorMessage = response.errorBody()?.string().orEmpty()
                NetworkResult.Error(errorMessage, response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error("Network error: ${e.message}")
        }
    }
}