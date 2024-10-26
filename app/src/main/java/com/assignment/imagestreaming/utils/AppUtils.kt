package com.assignment.imagestreaming.utils

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object AppUtils {
   suspend fun compressImage(file: File, quality: Int): File {
       return withContext(Dispatchers.IO){
           val originalBitmap = BitmapFactory.decodeFile(file.path)


           val compressedFile = File(file.parent, "compressed_${file.name}")


           FileOutputStream(compressedFile).use { outputStream ->

               originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
           }

           originalBitmap.recycle()

           return@withContext compressedFile
       }

    }

     fun isAppInBackground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return true
        val packageName = context.packageName

        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                && appProcess.processName == packageName) {
                return false
            }
        }
        return true
    }
}