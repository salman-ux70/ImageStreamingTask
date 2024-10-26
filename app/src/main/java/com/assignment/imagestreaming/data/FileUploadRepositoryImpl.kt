package com.assignment.imagestreaming.data

import com.assignment.imagestreaming.data.service.FileUploadService
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class FileUploadRepositoryImpl @Inject constructor(
    private val fileUploadService: FileUploadService
) : FileUploadRepository {
    override suspend fun uploadFile(file: MultipartBody.Part): Response<ResponseBody> {
        return fileUploadService.uploadFile(file)
    }
}