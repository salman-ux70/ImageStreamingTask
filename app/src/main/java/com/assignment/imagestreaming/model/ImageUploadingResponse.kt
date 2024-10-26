package com.assignment.imagestreaming.model

data class ImageUploadingResponse(
    val autoDelete: Boolean,
    val created: String,
    val description: Any,
    val downloads: Int,
    val expires: String,
    val id: String,
    val key: String,
    val link: String,
    val maxDownloads: Int,
    val mimeType: String,
    val modified: String,
    val name: String,
    val nodeType: String,
    val path: String,
    val planId: Int,
    val `private`: Boolean,
    val screeningStatus: String,
    val size: Int,
    val status: Int,
    val success: Boolean,
    val title: Any
)