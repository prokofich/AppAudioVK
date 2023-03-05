package com.stacktivity.media.common

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import com.stacktivity.media.common.extensions.title
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Class that manages a connection to a [MediaBrowserServiceCompat] instance,
 * typically a [MusicService].
 *
 * To control playback, use [transportControls].
 *
 * Allows you to track the playback status,
 * as well as the metadata of the media file being played
 * using [playbackState] and [nowPlaying] flows, respectively.
 *
 * @param context applicationContext
 * @param serviceComponent [ComponentName] of service implementing [MediaBrowserServiceCompat]
 */
class MusicServiceConnection(context: Context, serviceComponent: ComponentName) {

    private val _playbackState = MutableStateFlow(EMPTY_PLAYBACK_STATE)
    val playbackState: StateFlow<PlaybackStateCompat> get() = _playbackState.asStateFlow()

    private val _nowPlaying = MutableStateFlow(NOTHING_PLAYING)
    val nowPlaying: StateFlow<MediaMetadataCompat> get() = _nowPlaying.asStateFlow()

    private var mediaController: MediaControllerCompat? = null
    val transportControls: MediaControllerCompat.TransportControls?
        get() = mediaController?.transportControls

    private var mediaBrowser = MediaBrowserCompat(
        context,
        serviceComponent,
        MediaBrowserConnectionCallback(context), null
    ).apply { connect() }


    private inner class MediaBrowserConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken)
                .apply { registerCallback(MediaControllerCallback()) }
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.tryEmit(state ?: EMPTY_PLAYBACK_STATE)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _nowPlaying.tryEmit(
                if (metadata?.title == null) {
                    NOTHING_PLAYING
                } else {
                    metadata
                }
            )
        }
    }

    companion object {
        @Volatile
        private var instance: MusicServiceConnection? = null

        fun getInstance(context: Context, serviceComponent: ComponentName) =
            instance ?: synchronized(this) {
                instance ?: MusicServiceConnection(context, serviceComponent)
                    .also { instance = it }
            }
    }
}

val EMPTY_PLAYBACK_STATE: PlaybackStateCompat = PlaybackStateCompat.Builder()
    .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
    .build()

val NOTHING_PLAYING: MediaMetadataCompat = MediaMetadataCompat.Builder()
    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "")
    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
    .build()
