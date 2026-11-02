package com.zpr.reminder.notification

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationHelperTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context: Context = instrumentation.targetContext

    @Before
    fun grantNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            instrumentation.uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
    }

    @Test
    fun showsUpdateNotification() {
        val notificationId = 2001
        val shown = NotificationHelper.showUpdateNotification(
            context,
            "RSS 更新提醒",
            "你订阅的示例科技发布了 3 篇新文章",
            notificationId
        )
        assertTrue(shown)

        val manager = context.getSystemService(NotificationManager::class.java)
        val active = manager.activeNotifications
        assertTrue(active.any { it.id == notificationId })

        manager.cancel(notificationId)
    }
}
