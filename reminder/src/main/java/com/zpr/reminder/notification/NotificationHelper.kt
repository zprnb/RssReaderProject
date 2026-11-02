package com.zpr.reminder.notification

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zpr.reminder.R

object NotificationHelper {

    const val UPDATE_NOTIFICATION_ID = 1001

    @SuppressLint("MissingPermission")
    fun showUpdateNotification(
        context: Context,
        title: String,
        content: String,
        notificationId: Int = UPDATE_NOTIFICATION_ID
    ): Boolean {
        NotificationChannels.createAll(context)

        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) {
            return false
        }

        val notification = NotificationCompat.Builder(
            context,
            NotificationChannels.REMINDER_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(buildContentIntent(context, notificationId))
            .build()

        manager.notify(notificationId, notification)
        return true
    }

    private fun buildContentIntent(context: Context, notificationId: Int): PendingIntent? {
        val launchIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?: return null
        launchIntent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        return PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
