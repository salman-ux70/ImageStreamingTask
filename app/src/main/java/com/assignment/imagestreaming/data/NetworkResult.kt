package com.assignment.imagestreaming.data

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val errorCode: Int? = null) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}