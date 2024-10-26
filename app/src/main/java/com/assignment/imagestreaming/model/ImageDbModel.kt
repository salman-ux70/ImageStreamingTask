package com.assignment.imagestreaming.model

import androidx.camera.core.processing.SurfaceProcessorNode.In
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File

@Entity(tableName = "image_db_table")
data class ImageDbModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0 ,
    val imageFile: File
)
