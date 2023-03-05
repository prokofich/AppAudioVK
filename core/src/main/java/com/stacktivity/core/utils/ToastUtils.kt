package com.stacktivity.core.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.annotation.StringRes

@IntDef(Toast.LENGTH_LONG, Toast.LENGTH_SHORT)
private annotation class ToastLength

fun Context.shortToast(@StringRes text: Int) {
    shortToast(getString(text))
}

fun Context.shortToast(text: String) {
    show(text, Toast.LENGTH_SHORT)
}

fun Context.longToast(@StringRes text: Int) {
    longToast(getString(text))
}

fun Context.longToast(text: String) {
    show(text, Toast.LENGTH_LONG)
}

private fun Context.makeToast(text: String, @ToastLength length: Int): Toast {
    return Toast.makeText(this, text, length)
}

private fun Context.show(text: String, @ToastLength length: Int) {
    makeToast(text, length).show()
}