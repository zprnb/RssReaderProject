package com.zpr.rssreader.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zpr.rssreader.R
import com.zpr.rssreader.databinding.FragmentSettingsBinding
import com.zpr.rssreader.work.RefreshScheduler

/**
 * 设置页：自动刷新开关、数据统计、手动维护与伴生模块入口。
 */
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var binding: FragmentSettingsBinding? = null
    private val viewModel: SettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentBinding = FragmentSettingsBinding.bind(view)
        binding = currentBinding

        viewModel.autoRefreshEnabled.observe(viewLifecycleOwner) { enabled ->
            currentBinding.autoRefreshSwitch.setOnCheckedChangeListener(null)
            currentBinding.autoRefreshSwitch.isChecked = enabled
            currentBinding.autoRefreshStatusText.text = getString(
                if (enabled) R.string.auto_refresh_on else R.string.auto_refresh_off
            )
            currentBinding.autoRefreshSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.setAutoRefresh(isChecked)
                if (isChecked) {
                    RefreshScheduler.schedulePeriodic(requireContext())
                } else {
                    RefreshScheduler.cancelPeriodic(requireContext())
                }
                currentBinding.autoRefreshStatusText.text = getString(
                    if (isChecked) R.string.auto_refresh_on else R.string.auto_refresh_off
                )
            }
        }

        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            currentBinding.statsText.text = getString(
                R.string.settings_stats,
                stats.feedCount,
                stats.itemCount,
                stats.unreadCount
            )
        }

        viewModel.message.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrBlank()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.clearMessage()
            }
        }

        currentBinding.refreshNowButton.setOnClickListener {
            Toast.makeText(requireContext(), R.string.refreshing, Toast.LENGTH_SHORT).show()
            viewModel.refreshNow()
        }
        currentBinding.clearReadButton.setOnClickListener {
            viewModel.clearReadItems()
        }
        currentBinding.openReminderButton.setOnClickListener {
            openReminderCenter()
        }
    }

    private fun openReminderCenter() {
        val intent = requireContext().packageManager
            .getLaunchIntentForPackage(REMINDER_PACKAGE)
        if (intent == null) {
            Toast.makeText(
                requireContext(),
                R.string.companion_not_installed,
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        runCatching { startActivity(intent) }.onFailure {
            Toast.makeText(
                requireContext(),
                R.string.companion_not_installed,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    companion object {
        private const val REMINDER_PACKAGE = "com.zpr.reminder"
    }
}
