package com.stacktivity.voicenotes.repo

import com.stacktivity.voicenotes.model.VoiceNoteItem
import java.io.File

interface VoiceNotesRepository {

    fun getNewFile(extension: String? = null): File

    fun renameFile(from: String, to: String, extension: String? = null)

    fun fetchVoiceNotes(): List<VoiceNoteItem>

    fun fetchVoiceNote(name: String, extension: String? = null): VoiceNoteItem?

    suspend fun saveToRemote(name: String, extension: String? = null): Boolean

    companion object {
        fun getInstance(cacheDir: File): VoiceNotesRepository {
            return VoiceNotesRepositoryImpl(cacheDir)
        }
    }
}