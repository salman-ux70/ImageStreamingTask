package com.assignment.imagestreaming.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assignment.imagestreaming.data.FileUploadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject
@HiltViewModel
class FileUploadViewModel @Inject constructor(
    private val fileRepository: FileUploadRepository
) : ViewModel() {

    fun uploadImage(file: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                val response = fileRepository.uploadFile(file)
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    Log.d("Upload successful:","$$responseBody")
                } else {
                    Log.d("Upload failed:","${response.errorBody()?.string()}")

                }
            } catch (e: Exception) {
                println("Error uploading file: ${e.message}")
            }
        }
    }

}