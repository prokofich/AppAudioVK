package com.stacktivity.voicenotes.repo

import com.stacktivity.voicenotes.model.VkFile
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.internal.ApiCommand
import com.vk.sdk.api.docs.DocsService
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


object VkRepository {

    private val remoteScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    suspend fun saveToDocs(file: File) = suspendCancellableCoroutine<Boolean> { cont ->
        remoteScope.launch {
            runCatching {
                val uploadUrl = VK.enqueue(DocsService().docsGetUploadServer()).uploadUrl
                val body = uploadFile(file, uploadUrl)!!
                val vkFile = Json.decodeFromString<VkFile>(body).file

                VK.enqueue(DocsService().docsSave(vkFile, file.name))
                cont.resumeWith(Result.success(true))
            }.onFailure {
                it.printStackTrace()
                cont.resumeWith(Result.success(false))
            }
        }
    }

    private suspend fun uploadFile(
        file: File,
        uploadUrl: String
    ): String? = withContext(Dispatchers.IO) {
        val formBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, file.asRequestBody())
            .build()

        val call = OkHttpClient().newCall(
            Request.Builder()
                .post(formBody)
                .url(uploadUrl)
                .build()
        )

        return@withContext call.execute().body?.string()
    }
}

private suspend fun <T> VK.enqueue(request: ApiCommand<T>) = suspendCancellableCoroutine<T> { cont ->
    execute(request, object : VKApiCallback<T> {
        override fun fail(error: Exception) {
            cont.resumeWithException(error)
        }

        override fun success(result: T) {
            cont.resume(result)
        }
    })
}