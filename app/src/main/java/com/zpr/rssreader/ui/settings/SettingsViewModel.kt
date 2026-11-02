package com.zpr.rssreader.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.zpr.rssreader.R
import com.zpr.rssreader.RssApplication
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * 设置页统计信息、自动刷新开关与数据维护操作。
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as RssApplication).container
    private val feedRepository = container.feedRepository
    private val itemRepository = container.itemRepository
    private val syncRepository = container.syncRepository

    val stats = combine(
        feedRepository.observeFeeds(),
        itemRepository.observeAllItems(),
        itemRepository.observeUnreadCount()
    ) { feeds, items, unread ->
        SettingsStats(
            feedCount = feeds.size,
            itemCount = items.size,
            unreadCount = unread
        )
    }.asLiveData()

    val autoRefreshEnabled = MutableLiveData(container.preferences.autoRefreshEnabled)

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    fun setAutoRefresh(enabled: Boolean) {
        container.preferences.autoRefreshEnabled = enabled
        autoRefreshEnabled.value = enabled
    }

    fun refreshNow() {
        viewModelScope.launch {
            try {
                val summary = syncRepository.refreshAllFeeds()
                _message.value = if (summary.total == 0) {
                    getApplication<Application>().getString(R.string.refresh_no_feeds)
                } else {
                    getApplication<Application>().getString(
                        R.string.refresh_result,
                        summary.success,
                        summary.failed,
                        summary.insertedCount
                    )
                }
            } catch (e: Exception) {
                _message.value = e.message
                    ?: getApplication<Application>().getString(R.string.refresh_failed)
            }
        }
    }

    fun clearReadItems() {
        viewModelScope.launch {
            val count = itemRepository.deleteReadItems()
            _message.value = if (count == 0) {
                getApplication<Application>().getString(R.string.clear_read_empty)
            } else {
                getApplication<Application>().getString(
                    R.string.deleted_read_items,
                    count
                )
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

data class SettingsStats(
    val feedCount: Int,
    val itemCount: Int,
    val unreadCount: Int
)
