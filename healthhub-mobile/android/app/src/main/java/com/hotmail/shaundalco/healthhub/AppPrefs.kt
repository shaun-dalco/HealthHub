package com.hotmail.shaundalco.healthhub

import android.content.Context
import androidx.preference.PreferenceManager

object AppPrefs {
    private const val KEY_SERVER_URL = "server_url"

    fun getServerUrl(context: Context): String? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val url = prefs.getString(KEY_SERVER_URL, "https://example.com/steps/upload")?.trim()
        return url?.takeIf { it.isNotEmpty() }
    }
}
