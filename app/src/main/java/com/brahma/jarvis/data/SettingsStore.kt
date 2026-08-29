package com.brahma.jarvis.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Stores the user's Gemini API key and a couple of assistant preferences
 * encrypted on-device. Nothing here is ever sent anywhere except directly
 * to the Gemini API endpoint when a request is made.
 */
class SettingsStore(context: Context) {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "brahma_jarvis_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    var geminiApiKey: String
        get() = prefs.getString(KEY_GEMINI_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_GEMINI_API_KEY, value).apply()

    var assistantName: String
        get() = prefs.getString(KEY_ASSISTANT_NAME, "Brahma") ?: "Brahma"
        set(value) = prefs.edit().putString(KEY_ASSISTANT_NAME, value).apply()

    var voiceReplyEnabled: Boolean
        get() = prefs.getBoolean(KEY_VOICE_REPLY, true)
        set(value) = prefs.edit().putBoolean(KEY_VOICE_REPLY, value).apply()

    fun hasApiKey(): Boolean = geminiApiKey.isNotBlank()

    companion object {
        private const val KEY_GEMINI_API_KEY = "gemini_api_key"
        private const val KEY_ASSISTANT_NAME = "assistant_name"
        private const val KEY_VOICE_REPLY = "voice_reply_enabled"
    }
}
