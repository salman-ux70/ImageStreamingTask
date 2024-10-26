package com.assignment.imagestreaming.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.assignment.imagestreaming.model.ImageDbModel
import java.io.File

@Dao
interface ImageDao {

    @Insert
    suspend fun saveImageFile(item: ImageDbModel)

    @Query("SELECT * FROM IMAGE_DB_TABLE")
    fun getImagesFilesFromDb(): List<ImageDbModel>
}