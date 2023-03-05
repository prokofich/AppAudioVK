package com.stacktivity.voicenotes.adapter

import android.os.CountDownTimer
import android.view.View
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.stacktivity.voicenotes.databinding.VoiceNoteItemBinding
import com.stacktivity.voicenotes.model.VoiceNoteItem
import com.stacktivity.voicenotes.utils.PlayerProgressProvider


class VoiceNoteViewHolder(
    private val binding: VoiceNoteItemBinding,
) : ViewHolder(binding.root) {

    var item: VoiceNoteItem? = null
        private set
    val playButton get() = binding.btnPlay

    private var handleProgressTimer: CountDownTimer? = null


    fun onBind(item: VoiceNoteItem) {
        this.item = item

        binding.apply {
            title.text = item.title
            createTime.text = item.createTimeString
            totalTime.text = item.durationString
            btnPlay.isChecked = item.isPlaying
        }
    }

    fun handleMediaProgress(provider: PlayerProgressProvider) {
        val totalDurationMs = item!!.durationMs
        val startPlaybackPosition = provider.getCurrentPlaybackPositionMs()
        val countDownInterval = totalDurationMs / 25
        val millisInFuture = totalDurationMs - startPlaybackPosition

        binding.btnPlay.isChecked = true

        handleProgressTimer = object : CountDownTimer(millisInFuture, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                val playbackPosition = provider.getCurrentPlaybackPositionMs()
                showMediaProgress(
                    provider.getCurrentProgress(totalDurationMs, playbackPosition),
                    playbackPosition,
                )
            }

            override fun onFinish() {}

        }.start()
    }

    fun showMediaProgress(progress: Int, timeMs: Long) {
        binding.playerProgressBar.progress = progress
        binding.currentTime.text = VoiceNoteItem.timeSecondsToString((timeMs / 1000).toInt())

        if (binding.playerProgressBar.visibility != View.VISIBLE) {
            binding.apply {
                currentTime.visibility = View.VISIBLE
                timeDelimiter.visibility = View.VISIBLE
                playerProgressBar.visibility = View.VISIBLE
            }
        }
    }

    fun pauseHandleMediaProgress() {
        handleProgressTimer?.cancel()
        handleProgressTimer = null

        binding.btnPlay.isChecked = false
    }

    fun stopHandleMediaProgress() {
        pauseHandleMediaProgress()

        binding.apply {
            currentTime.visibility = View.INVISIBLE
            timeDelimiter.visibility = View.INVISIBLE
            playerProgressBar.visibility = View.INVISIBLE
            btnPlay.isChecked = false
        }
    }
}
