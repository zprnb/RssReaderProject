package com.zpr.rssreader.data.preferences

import android.content.Context

class AppPreferences(context: Context) {

    private val preferences = context.applicationContext
        .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    var autoRefreshEnabled: Boolean
        get() = preferences.getBoolean(KEY_AUTO_REFRESH, true)
        set(value) {
            preferences.edit().putBoolean(KEY_AUTO_REFRESH, value).apply()
        }

    companion object {
        private const val FILE_NAME = "rss_reader_preferences"
        private const val KEY_AUTO_REFRESH = "auto_refresh_enabled"
    }
}
