package com.stacktivity.core.utils

import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Used for safe operation with the application cache.
 * If there is not enough free space, it deletes older files.
 */
class CacheWorker(private val cacheDir: File) {
    private var tempFile: File? = null

    fun getFile(fileName: String, dir: String? = null): File {
        if (dir != null) {
            val fileDir = checkDir(dir)

            return File(fileDir.path + File.separator + fileName)
        }

        return File(cacheDir.path + File.separator + fileName)
    }

    fun getNewFile(extension: String? = null, dir: String? = null): File {
        val name = SimpleDateFormat("ddMMyy_HHmmss", Locale.getDefault()).format(Date()).let {
            if (extension != null) it.plus(".$extension")
            else it
        }
        return getFile(name, dir).apply { createNewFile() }
    }

    /**
     * Used to get a temp file that exists before this method is called again
     */
    fun getTempFile(): File {
        tempFile?.delete()
        return getNewFile().also { tempFile = it }
    }

    /**
     * Save [kotlin.ByteArray] to file
     *
     * @return false in case of an [IOException]
     */
    fun saveBytesToFile(buffer: ByteArray, file: File): Boolean {
        var res = false
        val currentFreeSpace = cacheDir.freeSpace

        if (currentFreeSpace > buffer.size) {
            res = saveBytes(buffer, file)
        } else if (clearCache(buffer.size - currentFreeSpace)) {
            res = saveBytes(buffer, file)
        }

        return res
    }

    private fun saveBytes(buffer: ByteArray, file: File): Boolean {
        var res = false

        try {
            file.writeBytes(buffer)
            res = true
        } catch (e: IOException) {
            // res = false
            e.printStackTrace()
        }

        return res
    }

    /**
     * Save [ByteBuffer] to file
     *
     * @return false in case of an [IOException]
     */
    fun saveBytesToFile(buffer: ByteBuffer, file: File) {
        buffer.rewind()
        val array = ByteArray(buffer.remaining())
        buffer.get(array)
        saveBytesToFile(array, file)
    }

    fun getListFiles(dir: String? = null): List<File> {
        return if (dir != null) {
            val fileDir = checkDir(dir)
            fileDir.listFiles()?.filter { it.isFile } ?: emptyList()
        } else cacheDir.listFiles()!!.filter { it.isFile }
    }

    private fun checkDir(dir: String): File {
        val fileDir = File(cacheDir.path + File.separator + dir)
        if (fileDir.exists().not()) {
            fileDir.mkdir()
        }

        return fileDir
    }

    /**
     * Deletes oldest files in cache until [byteCount] are cleared
     */
    fun clearCache(byteCount: Long): Boolean {
        var clearBytes = 0L
        return cacheDir.listFiles()?.let {
            it.sortBy { file -> file.lastModified() }

            for (file in it) {
                if (clearBytes > byteCount) break
                clearBytes += file.length()
                file.delete()
            }
            true
        } ?: false
    }

    fun clearAllCache() {
        cacheDir.listFiles()?.forEach { file ->
            file.delete()
        }
    }
}