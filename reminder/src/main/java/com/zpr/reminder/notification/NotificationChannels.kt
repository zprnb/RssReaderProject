package com.zpr.reminder.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.zpr.reminder.R

object NotificationChannels {

    const val REMINDER_CHANNEL_ID = "rss_reminders"

    fun createAll(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(REMINDER_CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            REMINDER_CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_description)
            enableVibration(true)
            setShowBadge(true)
        }

        manager.createNotificationChannel(channel)
    }
}
