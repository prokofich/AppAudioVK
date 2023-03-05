package com.stacktivity.vkvoicenotes

import android.app.Application
import android.content.Context
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKTokenExpiredHandler

class App : Application() {

    private val tokenTracker = object: VKTokenExpiredHandler {
        override fun onTokenExpired() {
            val preferences = getSharedPreferences(packageName, Context.MODE_PRIVATE)

            preferences.edit().putBoolean("TokenExpired", true).apply()
        }
    }

    override fun onCreate() {
        super.onCreate()

        VK.addTokenExpiredHandler(tokenTracker)
    }
}