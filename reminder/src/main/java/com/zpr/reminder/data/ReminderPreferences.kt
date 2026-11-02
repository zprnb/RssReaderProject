package com.zpr.reminder.data

import android.content.Context

class ReminderPreferences(context: Context) {

    private val preferences = context.applicationContext
        .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    var reminderEnabled: Boolean
        get() = preferences.getBoolean(KEY_REMINDER_ENABLED, true)
        set(value) {
            preferences.edit().putBoolean(KEY_REMINDER_ENABLED, value).apply()
        }

    var checkIntervalMinutes: Int
        get() = preferences.getInt(KEY_INTERVAL_MINUTES, DEFAULT_INTERVAL_MINUTES)
            .coerceIn(MIN_INTERVAL_MINUTES, MAX_INTERVAL_MINUTES)
        set(value) {
            preferences.edit()
                .putInt(
                    KEY_INTERVAL_MINUTES,
                    value.coerceIn(MIN_INTERVAL_MINUTES, MAX_INTERVAL_MINUTES)
                )
                .apply()
        }

    var lastCheckedAt: Long
        get() = preferences.getLong(KEY_LAST_CHECKED_AT, 0L)
        set(value) {
            preferences.edit().putLong(KEY_LAST_CHECKED_AT, value).apply()
        }

    var lastNotificationAt: Long
        get() = preferences.getLong(KEY_LAST_NOTIFICATION_AT, 0L)
        set(value) {
            preferences.edit().putLong(KEY_LAST_NOTIFICATION_AT, value).apply()
        }

    companion object {
        const val MIN_INTERVAL_MINUTES = 15
        const val MAX_INTERVAL_MINUTES = 1440
        const val DEFAULT_INTERVAL_MINUTES = 60

        private const val FILE_NAME = "reminder_preferences"
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
        private const val KEY_INTERVAL_MINUTES = "check_interval_minutes"
        private const val KEY_LAST_CHECKED_AT = "last_checked_at"
        private const val KEY_LAST_NOTIFICATION_AT = "last_notification_at"
    }
}
