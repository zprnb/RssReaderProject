package com.zpr.reminder.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zpr.reminder.data.ReminderPreferences
import com.zpr.reminder.data.RssArticleClient
import com.zpr.reminder.notification.NotificationHelper

class ReminderCheckWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val preferences = ReminderPreferences(applicationContext)
        if (!preferences.reminderEnabled) {
            return Result.success()
        }

        val now = System.currentTimeMillis()
        val since = preferences.lastCheckedAt

        val articles = RssArticleClient(applicationContext).queryNewArticles(since)
            ?: return Result.success()

        if (articles.isEmpty()) {
            preferences.lastCheckedAt = now
            return Result.success()
        }

        val shown = NotificationHelper.showNewArticlesNotification(
            applicationContext,
            articles
        )
        if (shown) {
            preferences.lastCheckedAt = now
            preferences.lastNotificationAt = now
        }
        return Result.success()
    }

    companion object {
        const val PERIODIC_WORK_NAME = "reminder_check_rss"
        const val ONE_TIME_WORK_NAME = "reminder_check_now"
    }
}
