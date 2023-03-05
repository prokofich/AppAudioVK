package com.stacktivity.voicenotes.ui.voicenotes.viewmodel.usecases

import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.stacktivity.media.common.MusicServiceConnection
import com.stacktivity.media.common.extensions.isPlaying
import com.stacktivity.media.common.extensions.isPrepared
import com.stacktivity.media.common.extensions.mediaUriPath
import com.stacktivity.voicenotes.model.PlayableItem
import com.stacktivity.voicenotes.model.PlayableItem.PlaybackState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.File


internal class PlayMediaUseCase<out T : List<PlayableItem>>(
    private val musicServiceConnection: MusicServiceConnection,
    private val mediaItemsFlow: MutableStateFlow<T>,
    executionScope: CoroutineScope,
) {

    private val executionScope = CoroutineScope(executionScope.coroutineContext + Dispatchers.IO)

    private var currentMediaItem: PlayableItem? = null

    init {
        setupPlayerObservers()
    }

    /**
     * This method takes a [PlayableItem] and does one of the following:
     * - If the item is *not* the active item, then play it directly.
     * - If the item *is* the active item, then pause playback,
     * otherwise send "play" to resume playback.
     */
    fun play(mediaItem: PlayableItem) {
        val nowPlaying = musicServiceConnection.nowPlaying.value
        val playbackState = musicServiceConnection.playbackState.value

        musicServiceConnection.transportControls?.let { transportControls ->
            if (playbackState.isPrepared && mediaItem.path == nowPlaying.mediaUriPath) {
                if (playbackState.isPlaying) {
                    transportControls.pause()
                } else transportControls.play()
            } else {
                transportControls.playFromUri(Uri.fromFile(File(mediaItem.path)), null)
            }
        }
    }

    fun applyCurrentState(items: List<PlayableItem>) {
        currentMediaItem?.let {
            items.applyState(it.state, it.title)
        }
    }


    /**
     * When the session's [PlaybackStateCompat] or [MediaMetadataCompat] changes,
     * the [mediaItemsFlow] need to be updated so the correct displayed active item.
     * (i.e.: [PlayableItem.state])
     */
    private fun setupPlayerObservers() {
        /*musicServiceConnection.nowPlaying.onEach { metadata ->
            val playbackState = musicServiceConnection.playbackState.value
            mediaItemsFlow.updateState(playbackState, metadata)
        }.launchIn(executionScope)*/

        musicServiceConnection.playbackState.onEach { playbackState ->
            val metadata = musicServiceConnection.nowPlaying.value
            mediaItemsFlow.updateState(playbackState, metadata)
        }.launchIn(executionScope)
    }


    /**
     * Metadata is considered empty if it does not contain the necessary data
     * to identify the item being played.
     */
    private fun MediaMetadataCompat.isEmpty(): Boolean {
        val itemPath = mediaUriPath
        return itemPath == null || itemPath.isEmpty()
    }

    private fun List<PlayableItem>.applyState(state: PlaybackState, title: String) {
        currentMediaItem = first { it.title == title }.also { it.state = state }
    }

    /**
     * @param playbackState
     * @param metadata
     */
    @Synchronized
    private fun MutableStateFlow<T>.updateState(
        playbackState: PlaybackStateCompat,
        metadata: MediaMetadataCompat
    ) {
        if (metadata.isEmpty()) return

        var newMediaItem: PlayableItem? = null

        @Suppress("UNCHECKED_CAST")
        tryEmit(value.map { item ->
            if (item.path == metadata.mediaUriPath) {
                val currentState = getPlayableItemState(playbackState)
                item.copy(currentState).also { newMediaItem = it }
            } else if (item != currentMediaItem) {
                item
            } else item.copy(PlaybackState.STOPPED)

        } as T)

        currentMediaItem = newMediaItem
    }

    private fun getPlayableItemState(playbackState: PlaybackStateCompat): PlaybackState =
        when (playbackState.state) {
            PlaybackStateCompat.STATE_PAUSED -> PlaybackState.PAUSED
            PlaybackStateCompat.STATE_PLAYING -> PlaybackState.PLAYING
            else -> PlaybackState.STOPPED
        }
}