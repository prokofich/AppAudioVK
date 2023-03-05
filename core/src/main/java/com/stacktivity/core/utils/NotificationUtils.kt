package com.stacktivity.core.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.IntDef
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes

object NotificationUtils {
    @RequiresApi(Build.VERSION_CODES.N)
    @MustBeDocumented
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(
        NotificationManager.IMPORTANCE_UNSPECIFIED,
        NotificationManager.IMPORTANCE_NONE,
        NotificationManager.IMPORTANCE_MIN,
        NotificationManager.IMPORTANCE_LOW,
        NotificationManager.IMPORTANCE_DEFAULT,
        NotificationManager.IMPORTANCE_HIGH
    )
    annotation class Importance

    fun createNotificationChannel(
        context: Context,
        id: String,
        @StringRes nameResourceId: Int,
        @StringRes descriptionResourceId: Int,
        @Importance importance: Int = NotificationManager.IMPORTANCE_LOW) {
        if (Build.VERSION.SDK_INT >= 26) {
            val notificationManager = checkNotNull(context.getSystemService(Context.NOTIFICATION_SERVICE)) as NotificationManager
            val channel = NotificationChannel(id, context.getString(nameResourceId), importance)
            if (descriptionResourceId != 0) {
                channel.description = context.getString(descriptionResourceId)
            }
            notificationManager.createNotificationChannel(channel);
        }
    }
}