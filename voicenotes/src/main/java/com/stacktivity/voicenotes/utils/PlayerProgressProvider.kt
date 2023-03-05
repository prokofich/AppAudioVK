package com.stacktivity.voicenotes.utils

interface PlayerProgressProvider {
    fun getCurrentPlaybackPositionMs(): Long
    fun getCurrentProgress(totalDurationMs: Long, playbackPositionMs: Long? = null): Int
}