package com.zpr.reminder

import android.app.Application
import com.zpr.reminder.data.ReminderPreferences
import com.zpr.reminder.notification.NotificationChannels
import com.zpr.reminder.work.ReminderScheduler

class ReminderApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createAll(this)

        val preferences = ReminderPreferences(this)
        if (preferences.reminderEnabled) {
            ReminderScheduler.schedulePeriodic(this, preferences.checkIntervalMinutes)
        }
    }
}
