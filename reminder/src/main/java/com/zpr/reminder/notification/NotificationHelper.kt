package com.zpr.reminder.notification

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zpr.reminder.R
import com.zpr.reminder.data.RemoteArticle

/**
 * 通知构建入口：测试通知与新文章提醒，统一使用 RSS 提醒渠道。
 */
object NotificationHelper {

    const val UPDATE_NOTIFICATION_ID = 1001
    const val NEW_ARTICLES_NOTIFICATION_ID = 1002

    private const val MAIN_APP_PACKAGE = "com.zpr.rssreader"
    private const val MAX_INBOX_LINES = 5
    private const val REQUEST_CODE_MARK_READ = 2001
    private const val REQUEST_CODE_SNOOZE = 2002
    private const val REQUEST_CODE_IGNORE = 2003

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

    /**
     * 展示新文章通知，附带“全部已读 / 稍后提醒 / 忽略”三个快捷操作。
     * 返回 false 表示系统通知权限未开启或通知被系统关闭。
     */
    @SuppressLint("MissingPermission")
    fun showNewArticlesNotification(
        context: Context,
        articles: List<RemoteArticle>
    ): Boolean {
        if (articles.isEmpty()) return false

        NotificationChannels.createAll(context)

        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) {
            return false
        }

        val count = articles.size
        val title = context.resources.getQuantityString(
            R.plurals.new_articles_title,
            count,
            count
        )
        val first = articles.first()
        val content = if (first.feedTitle.isBlank()) {
            first.title
        } else {
            "${first.feedTitle}：${first.title}"
        }

        val style = NotificationCompat.InboxStyle().setBigContentTitle(title)
        articles.take(MAX_INBOX_LINES).forEach { article ->
            style.addLine(
                if (article.feedTitle.isBlank()) {
                    article.title
                } else {
                    "${article.feedTitle}：${article.title}"
                }
            )
        }
        if (count > MAX_INBOX_LINES) {
            style.setSummaryText(
                context.getString(R.string.new_articles_more, count - MAX_INBOX_LINES)
            )
        }

        val notification = NotificationCompat.Builder(
            context,
            NotificationChannels.REMINDER_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(style)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setNumber(count)
            .setContentIntent(
                buildMainAppIntent(context, NEW_ARTICLES_NOTIFICATION_ID)
            )
            .addAction(
                R.drawable.ic_notification,
                context.getString(R.string.action_mark_read),
                actionPendingIntent(
                    context,
                    markReadIntent(context, articles),
                    REQUEST_CODE_MARK_READ
                )
            )
            .addAction(
                R.drawable.ic_notification,
                context.getString(R.string.action_snooze),
                actionPendingIntent(
                    context,
                    snoozeIntent(context, articles),
                    REQUEST_CODE_SNOOZE
                )
            )
            .addAction(
                R.drawable.ic_notification,
                context.getString(R.string.action_ignore),
                actionPendingIntent(
                    context,
                    ignoreIntent(context),
                    REQUEST_CODE_IGNORE
                )
            )
            .build()

        manager.notify(NEW_ARTICLES_NOTIFICATION_ID, notification)
        return true
    }

    private fun markReadIntent(context: Context, articles: List<RemoteArticle>): Intent {
        return Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_MARK_READ
            putExtra(
                NotificationActionReceiver.EXTRA_ARTICLE_IDS,
                articles.map { it.id }.toLongArray()
            )
        }
    }

    private fun snoozeIntent(context: Context, articles: List<RemoteArticle>): Intent {
        return Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra(
                NotificationActionReceiver.EXTRA_MIN_FETCHED_AT,
                articles.minOf { it.fetchedAt }
            )
        }
    }

    private fun ignoreIntent(context: Context): Intent {
        return Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_IGNORE
        }
    }

    private fun actionPendingIntent(
        context: Context,
        intent: Intent,
        requestCode: Int
    ): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildMainAppIntent(context: Context, notificationId: Int): PendingIntent? {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(MAIN_APP_PACKAGE)
            ?: return buildContentIntent(context, notificationId)
        launchIntent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        return PendingIntent.getActivity(
            context,
            notificationId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
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
