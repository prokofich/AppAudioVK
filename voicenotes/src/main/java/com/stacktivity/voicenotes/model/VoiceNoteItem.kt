package com.stacktivity.voicenotes.model

import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
import androidx.recyclerview.widget.DiffUtil
import org.ocpsoft.prettytime.PrettyTime
import java.io.File
import java.util.Locale
import java.util.Date
import com.stacktivity.voicenotes.model.PlayableItem.PlaybackState


data class VoiceNoteItem(
    override val title: String,
    val createTime: Long,
    val durationMs: Long,
    override val path: String,
    override var state: PlaybackState = PlaybackState.STOPPED
) : PlayableItem {

    val durationString: String = timeSecondsToString((durationMs / 1000).toInt())
    val createTimeString: String = PrettyTime(Locale.getDefault()).format(Date(createTime))


    constructor(mediaFile: File) : this(
        title = mediaFile.nameWithoutExtension,
        createTime = mediaFile.lastModified(),
        durationMs = getDurationData(mediaFile),
        path = mediaFile.absolutePath
    )


    override fun copy(newState: PlaybackState): PlayableItem {
        return copy(state = state).apply { this.state = newState }
    }


    companion object {
        fun getDiffCallback(): DiffUtil.ItemCallback<VoiceNoteItem> =
            object : DiffUtil.ItemCallback<VoiceNoteItem>() {
                override fun areItemsTheSame(old: VoiceNoteItem, new: VoiceNoteItem) =
                    old.id == new.id

                override fun areContentsTheSame(old: VoiceNoteItem, new: VoiceNoteItem) =
                    old == new && old.state == new.state

                override fun getChangePayload(old: VoiceNoteItem, new: VoiceNoteItem) =
                    if (old.state != new.state) new.state else super.getChangePayload(old, new)
            }

        fun timeSecondsToString(seconds: Int): String {
            val m = seconds / 60
            val s = seconds % 60
            return "$m:${if (s < 10) "0" else ""}$s"
        }

        private fun getDurationData(mediaFile: File): Long {
            return try {
                val dataRetriever = MediaMetadataRetriever().apply {
                    setDataSource(mediaFile.inputStream().fd)
                }
                dataRetriever.extractMetadata(METADATA_KEY_DURATION)!!.toLong()
                    .also { dataRetriever.release() }
            } catch (e: RuntimeException) {
                0
            }
        }
    }
}