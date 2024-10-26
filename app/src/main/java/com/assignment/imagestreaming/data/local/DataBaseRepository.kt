package com.assignment.imagestreaming.data.local

import com.assignment.imagestreaming.data.local.dao.ImageDao
import com.assignment.imagestreaming.model.ImageDbModel
import java.io.File
import javax.inject.Inject

class DataBaseRepository @Inject constructor(
    private val dao: ImageDao
) {
    suspend fun insertdata(item: ImageDbModel) {
        dao.saveImageFile(item)
    }

    suspend fun getAllImages() = dao.getImagesFilesFromDb()
}