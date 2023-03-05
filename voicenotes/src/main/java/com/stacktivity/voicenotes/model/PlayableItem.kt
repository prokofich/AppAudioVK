package com.stacktivity.voicenotes.model


interface PlayableItem {
    val title: String
    val path: String
    var state: PlaybackState

    val id get() = path.hashCode()
    val isPlaying get() = state == PlaybackState.PLAYING

    fun copy(newState: PlaybackState): PlayableItem

    enum class PlaybackState {
        PLAYING, PAUSED, STOPPED
    }
}
