package com.zpr.rssreader.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.zpr.rssreader.R
import com.zpr.rssreader.RssApplication
import com.zpr.rssreader.data.preferences.AppPreferences
import com.zpr.rssreader.databinding.FragmentSettingsBinding
import com.zpr.rssreader.work.RefreshScheduler

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var binding: FragmentSettingsBinding? = null
    private lateinit var preferences: AppPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentBinding = FragmentSettingsBinding.bind(view)
        binding = currentBinding

        val application = requireActivity().application as RssApplication
        preferences = application.container.preferences

        currentBinding.autoRefreshSwitch.isChecked = preferences.autoRefreshEnabled
        updateStatus(preferences.autoRefreshEnabled)

        currentBinding.autoRefreshSwitch.setOnCheckedChangeListener { _, isChecked ->
            preferences.autoRefreshEnabled = isChecked
            if (isChecked) {
                RefreshScheduler.schedulePeriodic(requireContext())
            } else {
                RefreshScheduler.cancelPeriodic(requireContext())
            }
            updateStatus(isChecked)
        }
    }

    private fun updateStatus(enabled: Boolean) {
        binding?.autoRefreshStatusText?.text = getString(
            if (enabled) R.string.auto_refresh_on else R.string.auto_refresh_off
        )
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
