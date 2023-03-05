package com.stacktivity.voicenotes.adapter

import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.stacktivity.media.common.EMPTY_PLAYBACK_STATE
import com.stacktivity.media.common.extensions.currentPlayBackPositionMs
import com.stacktivity.media.common.extensions.getCurrentProgress
import com.stacktivity.voicenotes.databinding.VoiceNoteItemBinding
import com.stacktivity.voicenotes.model.PlayableItem.PlaybackState
import com.stacktivity.voicenotes.model.VoiceNoteItem
import com.stacktivity.voicenotes.utils.PlayerProgressProvider


class VoiceNoteListAdapter(private val mediaItemClickListener: (VoiceNoteItem) -> Unit, ) : ListAdapter<VoiceNoteItem, VoiceNoteViewHolder>(VoiceNoteItem.getDiffCallback()),
    PlayerProgressProvider {

    private var playbackState: PlaybackStateCompat = EMPTY_PLAYBACK_STATE


    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): VoiceNoteViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = VoiceNoteItemBinding.inflate(inflater)

        return VoiceNoteViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: VoiceNoteViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            when (payloads.first() as PlaybackState) {
                PlaybackState.PLAYING -> holder.handleMediaProgress(this)
                PlaybackState.PAUSED -> holder.pauseHandleMediaProgress()
                PlaybackState.STOPPED -> holder.stopHandleMediaProgress()
            }
        } else super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindViewHolder(holder: VoiceNoteViewHolder, position: Int) {
        val voiceNoteItem = getItem(position)

        holder.playButton.setOnClickListener {
            it.isEnabled = false
            mediaItemClickListener(voiceNoteItem)
            it.isEnabled = true
        }

        holder.onBind(voiceNoteItem)


        when (voiceNoteItem.state) {
            PlaybackState.STOPPED -> {
            }
            PlaybackState.PLAYING -> holder.handleMediaProgress(this)
            PlaybackState.PAUSED -> {
                val currentPlaybackPosition = getCurrentPlaybackPositionMs()
                holder.showMediaProgress(
                    getCurrentProgress(voiceNoteItem.durationMs, currentPlaybackPosition),
                    currentPlaybackPosition
                )
            }
        }
    }

    override fun getItemCount() = currentList.size

    fun onPlaybackStateChanged(playbackState: PlaybackStateCompat) {
        this.playbackState = playbackState
    }


    override fun getCurrentPlaybackPositionMs(): Long {
        return playbackState.currentPlayBackPositionMs
    }

    override fun getCurrentProgress(totalDurationMs: Long, playbackPositionMs: Long?): Int {
        return playbackState.getCurrentProgress(totalDurationMs, playbackPositionMs)
    }

}
