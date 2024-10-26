package com.assignment.imagestreaming.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.assignment.imagestreaming.data.local.Converters
import com.assignment.imagestreaming.data.local.dao.ImageDao
import com.assignment.imagestreaming.model.ImageDbModel
import retrofit2.Converter

@Database(entities = [ImageDbModel::class], version = 1)
@TypeConverters(Converters::class)
abstract class ImageFilesDatabase : RoomDatabase(){
    abstract fun ImagesDao(): ImageDao
}