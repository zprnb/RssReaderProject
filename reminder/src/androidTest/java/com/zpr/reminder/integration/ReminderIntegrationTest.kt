package com.zpr.reminder.integration

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.StatusBarNotification
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zpr.reminder.data.ReminderPreferences
import com.zpr.reminder.data.RssArticleClient
import com.zpr.reminder.notification.NotificationHelper
import com.zpr.reminder.work.ReminderScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderIntegrationTest {

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
    fun detectsMainAppUpdatesAndPostsNotification() {
        val articles = RssArticleClient(context).queryNewArticles(0, 50).orEmpty()
        assumeTrue(articles.isNotEmpty())
        assumeTrue(articles.any { it.feedTitle.startsWith("集成测试订阅") })

        val preferences = ReminderPreferences(context)
        preferences.reminderEnabled = true
        preferences.lastCheckedAt = 0L

        ReminderScheduler.checkNow(context)

        val manager = context.getSystemService(NotificationManager::class.java)
        val deadline = System.currentTimeMillis() + 30_000L
        var record: StatusBarNotification? = null

        while (System.currentTimeMillis() < deadline) {
            record = manager.activeNotifications.firstOrNull {
                it.id == NotificationHelper.NEW_ARTICLES_NOTIFICATION_ID
            }
            if (record != null) break
            Thread.sleep(500)
        }

        assertNotNull(record)
        assertEquals(3, record!!.notification.actions.size)
        assertTrue(
            record.notification.actions.any {
                it.title.toString() == context.getString(com.zpr.reminder.R.string.action_mark_read)
            }
        )

        manager.cancel(NotificationHelper.NEW_ARTICLES_NOTIFICATION_ID)
    }
}
