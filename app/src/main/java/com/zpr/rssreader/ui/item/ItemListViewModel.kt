package com.zpr.rssreader.ui.item

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.zpr.rssreader.R
import com.zpr.rssreader.RssApplication
import kotlinx.coroutines.launch

/**
 * 文章列表状态，支持全部文章与指定订阅源两种模式。
 */
class ItemListViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as RssApplication).container
    private val itemRepository = container.itemRepository
    private val feedRepository = container.feedRepository
    private val syncRepository = container.syncRepository

    private val feedId = MutableLiveData<Long?>(null)

    val items = feedId.switchMap { id ->
        if (id == null || id <= 0L) {
            itemRepository.observeAllItems().asLiveData()
        } else {
            itemRepository.observeItemsByFeed(id).asLiveData()
        }
    }

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    fun setFeedId(id: Long?) {
        if (feedId.value != id) {
            feedId.value = id
        }
    }

    fun refresh() {
        if (_loading.value == true) return

        _loading.value = true
        viewModelScope.launch {
            try {
                val id = feedId.value
                if (id != null && id > 0L) {
                    val feed = feedRepository.getFeed(id)
                    if (feed == null) {
                        _message.value = getApplication<Application>()
                            .getString(R.string.refresh_failed)
                        return@launch
                    }
                    val inserted = syncRepository.refreshFeed(feed)
                    _message.value = getApplication<Application>().getString(
                        R.string.refresh_single_result,
                        inserted
                    )
                } else {
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
                }
            } catch (e: Exception) {
                _message.value = e.message
                    ?: getApplication<Application>().getString(R.string.refresh_failed)
            } finally {
                _loading.value = false
            }
        }
    }

    fun markAllRead() {
        viewModelScope.launch {
            itemRepository.markAllRead(feedId.value?.takeIf { it > 0L })
            _message.value = getApplication<Application>()
                .getString(R.string.mark_all_read_done)
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
