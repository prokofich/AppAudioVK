package com.stacktivity.media.common.extensions

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns


fun Uri.getName(context: Context): String? {
    var result: String? = null

    if (scheme.equals("content")) {
        val cursor: Cursor? = context.contentResolver
            .query(this, null, null, null, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                val columnId = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (columnId >= 0) {
                    result = cursor.getString(columnId)
                }
            }
        }
    }

    if (result == null) {
        result = path?.let {
            val cut = it.lastIndexOf('/')
            if (cut != -1) {
                it.substring(cut + 1)
            } else it

        }
    }

    return result
}

internal fun Uri.getFileNameWithoutExtension(context: Context): String? {
    return getName(context)?.substringBeforeLast(".")
}