package com.stacktivity.voicenotes.ui.voicenotes.viewmodel

import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stacktivity.media.common.MusicServiceConnection
import com.stacktivity.voicenotes.model.VoiceNoteItem
import com.stacktivity.voicenotes.repo.VoiceNotesRepository
import com.stacktivity.voicenotes.ui.voicenotes.viewmodel.usecases.PlayMediaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File


class VoiceNotesViewModel(
    private val repository: VoiceNotesRepository,
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    @Suppress("DEPRECATION")
    private var mediaRecorder: MediaRecorder = MediaRecorder()

    private val _voiceNotesFlow = MutableStateFlow<List<VoiceNoteItem>>(listOf())
    val voiceNotesFlow: StateFlow<List<VoiceNoteItem>> get() = _voiceNotesFlow.asStateFlow()

    private val _audioRecording = MutableStateFlow(false)
    val audioRecording: StateFlow<Boolean> get() = _audioRecording.asStateFlow()

    val playbackState get() = musicServiceConnection.playbackState

    private var recordingFile: File? = null
    private val voiceNoteFormat = "3gpp"

    private val playMediaUseCase =
        PlayMediaUseCase(musicServiceConnection, _voiceNotesFlow, viewModelScope)

    fun fetchItems() {
        viewModelScope.launch {
            val voiceNotes = repository.fetchVoiceNotes()
            playMediaUseCase.applyCurrentState(voiceNotes)

            _voiceNotesFlow.tryEmit(voiceNotes)
        }
    }

    fun recordAudio(): String {
        val audioFile = repository.getNewFile(voiceNoteFormat).also { recordingFile = it }
        val audioPath = audioFile.path
        viewModelScope.launch {
            initRecorder().apply {
                setOutputFile(audioPath)
                prepare()
                _audioRecording.value = true
                start()
            }
        }

        return audioFile.nameWithoutExtension
    }

    fun stopRecord() {
        mediaRecorder.apply {
            stop()
            _audioRecording.value = false
//            release()  // TODO
        }
    }

    fun applyRecordedAudioName(from: String, to: String) {
        viewModelScope.launch {
            if (from != to) {
                repository.renameFile(from, to, voiceNoteFormat)
            }

            repository.fetchVoiceNote(to, voiceNoteFormat)?.also { newNote ->
                ArrayList<VoiceNoteItem>(_voiceNotesFlow.value.size + 1)
                    .apply { add(newNote); addAll(_voiceNotesFlow.value) }
                    .also { _voiceNotesFlow.tryEmit(it) }
            }

            /*val saveResult = */repository.saveToRemote(to, voiceNoteFormat)
        }
    }

    fun onMediaItemClicked(item: VoiceNoteItem) {
        playMediaUseCase.play(item)
    }

    private fun initRecorder(): MediaRecorder {
        return mediaRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        }
    }
}