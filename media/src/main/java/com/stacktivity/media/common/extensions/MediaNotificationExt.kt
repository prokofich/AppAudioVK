package com.stacktivity.media.common.extensions

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.core.app.NotificationCompat


fun NotificationCompat.Builder.from(
    mediaController: MediaControllerCompat,
): NotificationCompat.Builder {
    val mediaMetadata: MediaMetadataCompat? = mediaController.metadata

    return this.apply {
        setContentIntent(mediaController.sessionActivity)
        setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (mediaMetadata != null) {
            val description = mediaMetadata.description

            setContentTitle(description.title)
            setContentText(description.subtitle)
            setSubText(description.description)
            setLargeIcon(description.iconBitmap)
        }
    }
}