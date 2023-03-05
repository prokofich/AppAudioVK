package com.stacktivity.voicenotes.repo

import com.stacktivity.core.utils.CacheWorker
import com.stacktivity.voicenotes.model.VoiceNoteItem
import java.io.File


private const val voiceNotesDir = "notes"

internal class VoiceNotesRepositoryImpl(cacheDir: File): VoiceNotesRepository {

    private val cacheWorker = CacheWorker(cacheDir)


    override fun getNewFile(extension: String?): File {
        return cacheWorker.getNewFile(extension = extension, dir = voiceNotesDir)
    }

    override fun renameFile(from: String, to: String, extension: String?) {
        val format = extension?.let { ".$extension" } ?: ""
        cacheWorker.getFile("$from$format", voiceNotesDir)
            .renameTo(cacheWorker.getFile("$to$format", voiceNotesDir))
    }

    override fun fetchVoiceNotes(): List<VoiceNoteItem> {
        return cacheWorker.getListFiles(voiceNotesDir)
            .map { VoiceNoteItem(it) }
            .sortedByDescending { it.createTime }
    }

    override fun fetchVoiceNote(name: String, extension: String?): VoiceNoteItem? {
        val format = extension?.let { ".$extension" } ?: ""
        val noteFile = cacheWorker.getFile("$name$format", voiceNotesDir)
        return if (noteFile.exists()) VoiceNoteItem(noteFile) else null
    }

    override suspend fun saveToRemote(name: String, extension: String?): Boolean {
        val format = extension?.let { ".$extension" } ?: ""
        val savingFile = cacheWorker.getFile("$name$format", voiceNotesDir)

        return VkRepository.saveToDocs(savingFile)
    }

}