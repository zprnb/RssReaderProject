package com.zpr.reminder.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.zpr.reminder.R
import com.zpr.reminder.data.ReminderPreferences
import com.zpr.reminder.databinding.ActivityMainBinding
import com.zpr.reminder.notification.NotificationChannels
import com.zpr.reminder.notification.NotificationHelper
import com.zpr.reminder.work.ReminderScheduler
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var reminderPreferences: ReminderPreferences
    private var pendingTestNotification = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        updateNotificationState()
        if (granted) {
            if (pendingTestNotification) {
                pendingTestNotification = false
                sendTestNotification()
            }
        } else {
            pendingTestNotification = false
            Toast.makeText(
                this,
                R.string.notification_permission_denied_toast,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NotificationChannels.createAll(this)

        reminderPreferences = ReminderPreferences(this)
        binding.reminderSwitch.isChecked = reminderPreferences.reminderEnabled
        binding.reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            reminderPreferences.reminderEnabled = isChecked
            if (isChecked) {
                ReminderScheduler.schedulePeriodic(
                    this,
                    reminderPreferences.checkIntervalMinutes
                )
            } else {
                ReminderScheduler.cancelPeriodic(this)
            }
            updateReminderState()
        }
        binding.checkNowButton.setOnClickListener {
            if (!reminderPreferences.reminderEnabled) {
                Toast.makeText(
                    this,
                    R.string.reminder_check_disabled,
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            ReminderScheduler.checkNow(this)
            Toast.makeText(
                this,
                R.string.reminder_check_started,
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.requestPermissionButton.setOnClickListener {
            pendingTestNotification = false
            requestNotificationPermission()
        }
        binding.openSettingsButton.setOnClickListener {
            openNotificationSettings()
        }
        binding.testNotificationButton.setOnClickListener {
            onTestNotificationClicked()
        }

        updateNotificationState()
        updateReminderState()
    }

    override fun onResume() {
        super.onResume()
        updateNotificationState()
        updateReminderState()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(
                this,
                R.string.notification_permission_not_required,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (isNotificationPermissionGranted()) {
            Toast.makeText(
                this,
                R.string.notification_permission_granted,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun updateReminderState() {
        binding.reminderStatusText.text = getString(
            if (reminderPreferences.reminderEnabled) {
                R.string.reminder_enabled
            } else {
                R.string.reminder_disabled
            }
        )

        val lastCheckedAt = reminderPreferences.lastCheckedAt
        binding.lastCheckText.text = if (lastCheckedAt <= 0L) {
            getString(R.string.reminder_last_check_none)
        } else {
            getString(R.string.reminder_last_check, formatTime(lastCheckedAt))
        }
    }

    private fun formatTime(timestamp: Long): String {
        val formatter = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd HH:mm",
            Locale.getDefault()
        )
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }

    private fun onTestNotificationClicked() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !isNotificationPermissionGranted()
        ) {
            pendingTestNotification = true
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        sendTestNotification()
    }

    private fun sendTestNotification() {
        val shown = NotificationHelper.showUpdateNotification(
            this,
            getString(R.string.test_notification_title),
            getString(R.string.test_notification_content)
        )
        Toast.makeText(
            this,
            if (shown) R.string.test_notification_sent else R.string.test_notification_blocked,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        runCatching { startActivity(intent) }.onFailure {
            Toast.makeText(
                this,
                R.string.notification_settings_unavailable,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateNotificationState() {
        val granted = isNotificationPermissionGranted()
        binding.permissionStatusText.text = getString(
            if (granted) {
                R.string.notification_permission_granted
            } else {
                R.string.notification_permission_denied
            }
        )
        binding.permissionStatusText.setTextColor(
            ContextCompat.getColor(
                this,
                if (granted) R.color.status_success else R.color.status_warning
            )
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            binding.requestPermissionButton.isEnabled = !granted
            binding.requestPermissionButton.text = getString(
                if (granted) {
                    R.string.notification_permission_granted
                } else {
                    R.string.notification_permission_request
                }
            )
        } else {
            binding.requestPermissionButton.isEnabled = false
            binding.requestPermissionButton.text = getString(
                R.string.notification_permission_not_required
            )
        }
    }

    private fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(this).areNotificationsEnabled()
        }
    }
}
