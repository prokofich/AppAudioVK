package com.stacktivity.voicenotes.ui.voicenotes

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.stacktivity.media.common.MusicService
import com.stacktivity.media.common.MusicServiceConnection
import com.stacktivity.voicenotes.repo.VoiceNotesRepository
import com.stacktivity.voicenotes.ui.voicenotes.viewmodel.VoiceNotesViewModel


class VoiceNotesViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val appContext = context.applicationContext

    private fun provideMusicServiceConnection(context: Context): MusicServiceConnection {
        return MusicServiceConnection.getInstance(
            context,
            ComponentName(context, MusicService::class.java)
        )
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = VoiceNotesRepository.getInstance(appContext.cacheDir)
        @Suppress("UNCHECKED_CAST")
        return VoiceNotesViewModel(repository, provideMusicServiceConnection(appContext)) as T
    }
}