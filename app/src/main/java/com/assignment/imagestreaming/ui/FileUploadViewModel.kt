package com.assignment.imagestreaming.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assignment.imagestreaming.data.FileUploadRepository
import com.assignment.imagestreaming.data.NetworkResult
import com.assignment.imagestreaming.model.ApiError
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class FileUploadViewModel @Inject constructor(
    private val fileRepository: FileUploadRepository
) : ViewModel() {
    private val _uploadResult = MutableStateFlow<NetworkResult<String>>(NetworkResult.Loading)
    val uploadResult: StateFlow<NetworkResult<String>> = _uploadResult

    fun uploadImage(context: Context, file: MultipartBody.Part) {
        viewModelScope.launch {
            _uploadResult.value = NetworkResult.Loading
            try {
                when (val response = fileRepository.uploadFile(file)) {
                    NetworkResult.Loading -> {
                        _uploadResult.value = NetworkResult.Loading

                    }

                    is NetworkResult.Success -> {
                        //this is to only demonstrate that our api call was successfull
                        _uploadResult.value = NetworkResult.Success("success")
                    }

                    is NetworkResult.Error -> {
                        val gson = Gson()
                        val errorMessage = response.message
                        try {
                            val apiError = gson.fromJson(errorMessage, ApiError::class.java)

                            val extractedMessage = apiError.message
                            _uploadResult.value = NetworkResult.Error(extractedMessage)
                        } catch (e: JsonSyntaxException) {
                            _uploadResult.value = NetworkResult.Error("something went wrong")
                        }

                    }
                }

            } catch (e: Exception) {
                _uploadResult.value = NetworkResult.Error(e.message.toString())

            }
        }
    }

}