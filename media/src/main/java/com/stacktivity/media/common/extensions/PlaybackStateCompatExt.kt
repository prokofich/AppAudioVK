/*
 * Copyright 2018 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("UNUSED")
package com.stacktivity.media.common.extensions

import android.os.SystemClock
import android.support.v4.media.session.PlaybackStateCompat

/**
 * Useful extension methods for [PlaybackStateCompat].
 */
inline val PlaybackStateCompat.isPrepared
    get() = (state == PlaybackStateCompat.STATE_BUFFERING) ||
            (state == PlaybackStateCompat.STATE_PLAYING) ||
            (state == PlaybackStateCompat.STATE_PAUSED)

inline val PlaybackStateCompat.isPlaying
    get() = (state == PlaybackStateCompat.STATE_BUFFERING) ||
            (state == PlaybackStateCompat.STATE_PLAYING)


inline val PlaybackStateCompat.stateName
    get() = when (state) {
        PlaybackStateCompat.STATE_NONE -> "STATE_NONE"
        PlaybackStateCompat.STATE_STOPPED -> "STATE_STOPPED"
        PlaybackStateCompat.STATE_PAUSED -> "STATE_PAUSED"
        PlaybackStateCompat.STATE_PLAYING -> "STATE_PLAYING"
        PlaybackStateCompat.STATE_FAST_FORWARDING -> "STATE_FAST_FORWARDING"
        PlaybackStateCompat.STATE_REWINDING -> "STATE_REWINDING"
        PlaybackStateCompat.STATE_BUFFERING -> "STATE_BUFFERING"
        PlaybackStateCompat.STATE_ERROR -> "STATE_ERROR"
        else -> "UNKNOWN_STATE"
    }

/**
 * Calculates the current playback position based on last update time along with playback
 * state and speed.
 */
inline val PlaybackStateCompat.currentPlayBackPositionMs: Long
    get() = if (state == PlaybackStateCompat.STATE_PLAYING) {
        val timeDelta = SystemClock.elapsedRealtime() - lastPositionUpdateTime
        (position + (timeDelta * playbackSpeed)).toLong()
    } else {
        position
    }

/**
 * Calculates the current progress based on [totalDurationMs]
 * and [playbackPositionMs] or current playback position, if it is not known
 *
 * @param totalDurationMs
 * @see currentPlayBackPositionMs
 */
fun PlaybackStateCompat.getCurrentProgress(totalDurationMs: Long, playbackPositionMs: Long?): Int {
    val position = playbackPositionMs ?: currentPlayBackPositionMs
    return (position.toDouble() / totalDurationMs * 100).toInt()
}

