package com.assignment.imagestreaming.data.local

import androidx.room.TypeConverter
import java.io.File

class Converters {
    @TypeConverter
    fun fromFile(file: File): String {
        return file.path
    }

    @TypeConverter
    fun toFile(path: String): File {
        return File(path)
    }
}
