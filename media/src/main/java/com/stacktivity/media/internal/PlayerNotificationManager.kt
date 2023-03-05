package com.stacktivity.media.internal

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Action
import androidx.core.app.NotificationManagerCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.stacktivity.core.utils.NotificationUtils
import com.stacktivity.media.R.drawable.ic_player_notification
import com.stacktivity.media.R.drawable.ic_player_notification_pause
import com.stacktivity.media.R.drawable.ic_player_notification_play
import com.stacktivity.media.R.string.player_notification_channel_title
import com.stacktivity.media.R.string.player_notification_channel_description
import com.stacktivity.media.common.extensions.from


private const val notificationId = 0xb340
private const val notificationChannel = "com.stacktivity.media.NOW_PLAYING"

/**
 * Starts, updates and cancels a media style notification reflecting the player state.
 *
 * The drawables used by PlayerNotificationManager can be overridden by drawables with the same names defined.
 * The drawables that can be overridden are:
 * - ic_player_notification - The icon passed by default to
 *                            NotificationCompat.Builder.setSmallIcon(int).
 * - ic_player_notification_play - The play icon.
 * - ic_player_notification_pause - The pause icon.
 */
internal class PlayerNotificationManager(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token,
    private val notificationListener: NotificationListener
) {

    interface NotificationListener {
        fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean)
        fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean)
    }


    private var isNotificationStarted = false

    private val notificationManager = NotificationManagerCompat.from(context)

    private val mediaController = MediaControllerCompat(context, sessionToken)

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_PLAY -> mediaController.transportControls.play()
                ACTION_PAUSE -> mediaController.transportControls.pause()
                ACTION_STOP -> stopNotification(true)
            }
        }
    }

    init {
        val intentFilter = IntentFilter().apply {
            addAction(ACTION_PLAY)
            addAction(ACTION_PAUSE)
            addAction(ACTION_STOP)
        }

        createNotificationChannel()
        setupObservers()

        context.registerReceiver(broadcastReceiver, intentFilter)
    }


    private fun setupObservers() {
        val callback = object : MediaControllerCompat.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                when (state?.state) {
                    PlaybackStateCompat.STATE_PLAYING,
                    PlaybackStateCompat.STATE_PAUSED -> {
                        startOrUpdateNotification(state.state)
                    }

                    else -> { /* Nothing to do */
                    }
                }
            }
        }

        mediaController.registerCallback(callback)
    }

    private fun startOrUpdateNotification(playbackState: Int) {
        if (playbackState == PlaybackStateCompat.STATE_STOPPED) {
            stopNotification(false)
            return
        }

        val ongoing = playbackState == PlaybackStateCompat.STATE_PLAYING
        val notification = getNotification(playbackState, context)

        notificationManager.notify(notificationId, notification)
        isNotificationStarted = true

        notificationListener.onNotificationPosted(
            notificationId, notification, ongoing || !isNotificationStarted
        )
    }

    private fun stopNotification(dismissedByUser: Boolean) {
        if (isNotificationStarted) {
            isNotificationStarted = false
            notificationManager.cancel(notificationId)
            notificationListener.onNotificationCancelled(notificationId, dismissedByUser)
        }
    }

    private fun getNotification(playbackState: Int, context: Context): Notification {
        val builder = NotificationCompat.Builder(context, notificationChannel)
            .from(mediaController)

        val mediaStyle = MediaStyle()
            .setShowActionsInCompactView(0)
            .setMediaSession(mediaController.sessionToken)
            .setCancelButtonIntent(getStopIntent())

        builder
            .setStyle(mediaStyle)
            .setSmallIcon(ic_player_notification)
            .setShowWhen(false)  // не отображать время создания уведомления
            .setOnlyAlertOnce(true)
            .addAction(getPlayPauseAction(playbackState))  // play/pause button
            .setOngoing(playbackState == PlaybackStateCompat.STATE_PLAYING)
            .setDeleteIntent(getStopIntent())
            .priority = NotificationCompat.PRIORITY_HIGH

        return builder.build()
    }

    private fun getPlayPauseAction(playbackState: Int): Action {
        val actionTitle: String
        val action: String
        val ic: Int

        if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
            actionTitle = "pause"
            action = ACTION_PAUSE
            ic = ic_player_notification_pause
        } else {
            actionTitle = "play"
            action = ACTION_PLAY
            ic = ic_player_notification_play
        }

        val intent: Intent = Intent(action).setPackage(context.packageName)

        val pendingFlags: Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        return Action(
            ic, actionTitle,
            PendingIntent.getBroadcast(context, 0, intent, pendingFlags)
        )
    }

    private fun getStopIntent(): PendingIntent {
        val intent: Intent = Intent(ACTION_STOP).setPackage(context.packageName)
        val pendingFlags: Int = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        return PendingIntent.getBroadcast(context, 0, intent, pendingFlags)
    }


    private fun createNotificationChannel() {
        NotificationUtils.createNotificationChannel(
            context,
            notificationChannel,
            player_notification_channel_title,
            player_notification_channel_description
        )
    }


    companion object {
        private const val ACTION_PLAY = "com.stacktivity.media.play"
        private const val ACTION_PAUSE = "com.stacktivity.media.pause"
        private const val ACTION_STOP = "com.stacktivity.media.stop"
    }
}
