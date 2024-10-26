package com.assignment.imagestreaming.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat


object PermissionUtils {
    fun hasCameraPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED


    }
}