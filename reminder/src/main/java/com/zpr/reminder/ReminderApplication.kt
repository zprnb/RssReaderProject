package com.zpr.reminder

import android.app.Application
import com.zpr.reminder.notification.NotificationChannels

class ReminderApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createAll(this)
    }
}
