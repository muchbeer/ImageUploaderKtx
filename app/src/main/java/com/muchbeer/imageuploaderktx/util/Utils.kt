package com.muchbeer.imageuploaderktx.util

import android.app.Activity
import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.view.View
import com.google.android.material.snackbar.Snackbar
import android.provider.MediaStore
import android.util.Log
import java.lang.Exception

private const val TAG = "Utils"
fun View.snackbar(message: String) {
    Snackbar.make(
        this,
        message,
        Snackbar.LENGTH_LONG
    ).also { snackbar ->
        snackbar.setAction("Ok") {
            snackbar.dismiss()
        }
    }.show()
}

 fun getRealPathFromURI(context: Activity, contentUri: Uri): String? {
    var cursor: Cursor? = null
    return try {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        cursor = context.getContentResolver().query(contentUri, proj, null, null, null)
        val column_index: Int = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        cursor.getString(column_index)
    } catch (e: Exception) {
        Log.e(TAG, "getRealPathFromURI Exception : $e")
        ""
    } finally {
        if (cursor != null) {
            cursor.close()
        }
    }
}