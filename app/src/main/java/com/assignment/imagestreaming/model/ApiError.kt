package com.assignment.imagestreaming.model

data class ApiError(
    val success: Boolean,
    val name: String,
    val code: String,
    val message: String,
    val status: Int,
    val help: String,
    val key: String
)