package com.agrismart.app.data.api

import android.content.Context
import com.agrismart.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiConfig @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("agrismart_api_config", Context.MODE_PRIVATE)

    var serverUrl: String
        get() {
            val saved = prefs.getString("custom_server_url", "") ?: ""
            if (saved.isNotBlank()) return saved
            val raw = BuildConfig.API_BASE_URL.trim()
            return if (raw.isNotBlank()) raw else "https://sih-2026-u4kv.onrender.com/"
        }
        set(value) {
            prefs.edit().putString("custom_server_url", value.trim()).apply()
        }

    var plantNetApiKey: String
        get() = prefs.getString("plantnet_api_key", "") ?: ""
        set(value) {
            prefs.edit().putString("plantnet_api_key", value.trim()).apply()
        }

    var groqApiKey: String
        get() = prefs.getString("groq_api_key", "") ?: ""
        set(value) {
            prefs.edit().putString("groq_api_key", value.trim()).apply()
        }

    var enableOnlineSearch: Boolean
        get() = prefs.getBoolean("enable_online_search", false)
        set(value) {
            prefs.edit().putBoolean("enable_online_search", value).apply()
        }

    var hasSelectedLanguage: Boolean
        get() = prefs.getBoolean("has_selected_language_v1", false)
        set(value) {
            prefs.edit().putBoolean("has_selected_language_v1", value).apply()
        }

    var useOfflineModel: Boolean
        get() = prefs.getBoolean("use_offline_model", false)
        set(value) {
            prefs.edit().putBoolean("use_offline_model", value).apply()
        }

    val isConfigured: Boolean
        get() = serverUrl.isNotBlank()

    val baseUrl: String
        get() {
            val url = serverUrl
            return if (url.endsWith("/")) url else "$url/"
        }
}
