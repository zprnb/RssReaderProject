package com.zpr.reminder.notification

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.WorkManager
import com.zpr.reminder.data.ReminderPreferences
import com.zpr.reminder.data.RemoteArticle
import com.zpr.reminder.data.RssArticleClient
import com.zpr.reminder.work.ReminderCheckWorker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class NotificationActionTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context: Context = instrumentation.targetContext

    @Before
    fun setUp() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            instrumentation.uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    @Test
    fun notificationHasActionButtons() {
        val articles = listOf(
            RemoteArticle(
                id = 1L,
                title = "测试标题",
                feedTitle = "示例科技",
                link = null,
                pubDate = 0L,
                fetchedAt = System.currentTimeMillis()
            )
        )
        assertTrue(NotificationHelper.showNewArticlesNotification(context, articles))

        val manager = context.getSystemService(NotificationManager::class.java)
        val record = manager.activeNotifications.firstOrNull {
            it.id == NotificationHelper.NEW_ARTICLES_NOTIFICATION_ID
        }
        assertNotNull(record)
        assertEquals(3, record!!.notification.actions.size)
        assertEquals("全部已读", record.notification.actions[0].title.toString())

        manager.cancel(NotificationHelper.NEW_ARTICLES_NOTIFICATION_ID)
    }

    @Test
    fun snoozeUpdatesLastCheckedAndSchedulesWork() {
        val preferences = ReminderPreferences(context)
        val fetchedAt = System.currentTimeMillis() - 1_000L
        preferences.lastCheckedAt = System.currentTimeMillis()

        context.sendBroadcast(
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_SNOOZE
                putExtra(NotificationActionReceiver.EXTRA_MIN_FETCHED_AT, fetchedAt)
            }
        )

        Thread.sleep(800)
        assertEquals(fetchedAt - 1L, preferences.lastCheckedAt)

        val infos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(ReminderCheckWorker.SNOOZE_WORK_NAME)
            .get(5, TimeUnit.SECONDS)
        assertTrue(infos.isNotEmpty())

        WorkManager.getInstance(context)
            .cancelUniqueWork(ReminderCheckWorker.SNOOZE_WORK_NAME)
    }

    @Test
    fun ignoreCancelsNotification() {
        NotificationHelper.showNewArticlesNotification(
            context,
            listOf(
                RemoteArticle(
                    id = 2L,
                    title = "忽略测试",
                    feedTitle = "示例科技",
                    link = null,
                    pubDate = 0L,
                    fetchedAt = System.currentTimeMillis()
                )
            )
        )

        context.sendBroadcast(
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_IGNORE
            }
        )

        Thread.sleep(800)
        val manager = context.getSystemService(NotificationManager::class.java)
        assertTrue(
            manager.activeNotifications.none {
                it.id == NotificationHelper.NEW_ARTICLES_NOTIFICATION_ID
            }
        )
    }

    @Test
    fun markReadUpdatesProvider() {
        val client = RssArticleClient(context)
        val before = client.queryNewArticles(0, 20).orEmpty()
        assumeTrue(before.isNotEmpty())

        val ids = before.take(2).map { it.id }
        context.sendBroadcast(
            Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_MARK_READ
                putExtra(
                    NotificationActionReceiver.EXTRA_ARTICLE_IDS,
                    ids.toLongArray()
                )
            }
        )

        Thread.sleep(1_500)
        val after = client.queryNewArticles(0, 20).orEmpty()
        assertTrue(after.none { it.id in ids })
    }
}
