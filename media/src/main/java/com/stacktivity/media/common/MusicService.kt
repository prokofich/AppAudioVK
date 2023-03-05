package com.stacktivity.media.common

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.stacktivity.media.internal.PlayerNotificationManager
import com.stacktivity.media.internal.PlayerNotificationManager.NotificationListener
import com.stacktivity.media.common.extensions.getFileNameWithoutExtension
import kotlin.properties.Delegates.notNull

/**
 * A simple music service that implements [MediaBrowserServiceCompat].
 * Used to play the selected audio recording from URI.
 * Is the entry point for playback commands from the APP's UI.
 * Supports PLAY/PAUSE actions.
 */
class MusicService : MediaBrowserServiceCompat() {

    private var mediaSession: MediaSessionCompat by notNull()

    private var notificationManager: PlayerNotificationManager by notNull()

    private var isForegroundService = false

    private val actions: Long = (PlaybackStateCompat.ACTION_PLAY
            or PlaybackStateCompat.ACTION_STOP
            or PlaybackStateCompat.ACTION_PAUSE
            or PlaybackStateCompat.ACTION_PLAY_PAUSE)

    private val stateBuilder = PlaybackStateCompat.Builder()
        .setActions(actions)

    private val playerListener = SessionEventListener()

    private val player: MediaPlayer by lazy {
        MediaPlayer().apply {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()

            setAudioAttributes(audioAttributes)
            setOnCompletionListener {
                stopForeground(true)
                isForegroundService = false
                mediaSession.isActive = false

                mediaSession.setPlaybackState(
                    stateBuilder
                        .setState(PlaybackStateCompat.STATE_STOPPED, 100, 1f)
                        .build()
                )
            }
        }
    }


    override fun onCreate() {
        super.onCreate()

        mediaSession = createMediaSession().also {
            sessionToken = it.sessionToken
            it.setCallback(playerListener)
        }

        notificationManager = PlayerNotificationManager(
            this, mediaSession.sessionToken, PlayerNotificationListener()
        )
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)

        player.stop()
        player.reset()
    }

    override fun onDestroy() {
        mediaSession.run {
            isActive = false
            release()
        }

        player.release()
    }

    // MediaBrowserServiceCompat
    override fun onGetRoot(
        clientPackageName: String, clientUid: Int, rootHints: Bundle?
    ) = BrowserRoot("@empty@", null)

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaItem>>) {
        result.sendResult(null)
    }


    /** Create a session with handling action of opening an parent Activity */
    private fun createMediaSession(): MediaSessionCompat {
        // Build a PendingIntent that can be used to launch the UI.
        val pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, pendingFlags)
            }

        return MediaSessionCompat(this, packageName + "MusicService")
            .apply { setSessionActivity(sessionActivityPendingIntent) }
    }


    /** Used to handle user actions. */
    private inner class SessionEventListener : MediaSessionCompat.Callback() {
        override fun onPlay() {
            mediaSession.setPlaybackState(
                stateBuilder
                    .setState(
                        PlaybackStateCompat.STATE_PLAYING,
                        player.currentPosition.toLong(), 1f
                    )
                    .build()
            )

            player.start()

            if (mediaSession.isActive.not()) {
                mediaSession.isActive = true
            }
        }

        override fun onPause() {
            stopForeground(false)
            isForegroundService = false

            player.pause()
            mediaSession.setPlaybackState(
                stateBuilder.setState(
                    PlaybackStateCompat.STATE_PAUSED,
                    player.currentPosition.toLong(), 1f
                ).build()
            )
        }

        override fun onPrepareFromUri(uri: Uri, extras: Bundle?) {
            player.stop()
            player.reset()

            val mediaTitle = uri.getFileNameWithoutExtension(applicationContext)

            val metadata = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mediaTitle)
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, uri.path)
                .build()

            mediaSession.setMetadata(metadata)

            player.setDataSource(applicationContext, uri)
            player.prepare()
        }

        override fun onPlayFromUri(uri: Uri, extras: Bundle?) {
            onPrepareFromUri(uri, extras)
            onPlay()
        }
    }

    /** Used to handle events of the player notification lifecycle */
    private inner class PlayerNotificationListener : NotificationListener {

        override fun onNotificationPosted(
            notificationId: Int,
            notification: Notification,
            ongoing: Boolean
        ) {
            if (ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(
                    applicationContext,
                    Intent(applicationContext, this@MusicService.javaClass)
                )

                startForeground(notificationId, notification)
                isForegroundService = true
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            stopForeground(true)
            isForegroundService = false

            mediaSession.setPlaybackState(
                stateBuilder
                    .setState(PlaybackStateCompat.STATE_STOPPED, 100, 1f)
                    .build()
            )

            stopSelf()
        }
    }
}
