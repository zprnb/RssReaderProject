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
import com.zpr.reminder.databinding.ActivityMainBinding
import com.zpr.reminder.notification.NotificationChannels

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        updateNotificationState()
        if (!granted) {
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

        binding.requestPermissionButton.setOnClickListener {
            requestNotificationPermission()
        }
        binding.openSettingsButton.setOnClickListener {
            openNotificationSettings()
        }

        updateNotificationState()
    }

    override fun onResume() {
        super.onResume()
        updateNotificationState()
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
