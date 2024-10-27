package com.assignment.imagestreaming.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import androidx.core.content.ContextCompat


fun Context.showSettings() {
    val intent: Intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", packageName, null)
    intent.setData(uri)
    startActivity(intent)
}

fun View.hide() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}