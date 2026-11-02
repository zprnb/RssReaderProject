package com.zpr.reminder.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.zpr.reminder.data.ReminderPreferences
import com.zpr.reminder.data.RssArticleClient
import com.zpr.reminder.work.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val manager = NotificationManagerCompat.from(context)

        when (intent.action) {
            ACTION_MARK_READ -> {
                val ids = intent.getLongArrayExtra(EXTRA_ARTICLE_IDS)
                    ?.toList()
                    .orEmpty()
                if (ids.isNotEmpty()) {
                    val pendingResult = goAsync()
                    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                        try {
                            RssArticleClient(context.applicationContext)
                                .markArticlesRead(ids)
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
                manager.cancel(NotificationHelper.NEW_ARTICLES_NOTIFICATION_ID)
            }

            ACTION_SNOOZE -> {
                val fetchedAt = intent.getLongExtra(EXTRA_MIN_FETCHED_AT, 0L)
                if (fetchedAt > 0L) {
                    ReminderPreferences(context.applicationContext).lastCheckedAt =
                        (fetchedAt - 1L).coerceAtLeast(0L)
                }
                ReminderScheduler.scheduleSnooze(
                    context.applicationContext,
                    SNOOZE_MINUTES
                )
                manager.cancel(NotificationHelper.NEW_ARTICLES_NOTIFICATION_ID)
            }

            ACTION_IGNORE -> {
                manager.cancel(NotificationHelper.NEW_ARTICLES_NOTIFICATION_ID)
            }
        }
    }

    companion object {
        const val ACTION_MARK_READ = "com.zpr.reminder.action.MARK_READ"
        const val ACTION_SNOOZE = "com.zpr.reminder.action.SNOOZE"
        const val ACTION_IGNORE = "com.zpr.reminder.action.IGNORE"
        const val EXTRA_ARTICLE_IDS = "extra_article_ids"
        const val EXTRA_MIN_FETCHED_AT = "extra_min_fetched_at"

        private const val SNOOZE_MINUTES = 15
    }
}
