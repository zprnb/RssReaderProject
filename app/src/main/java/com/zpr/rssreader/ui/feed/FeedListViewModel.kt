package com.zpr.rssreader.ui.feed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.zpr.rssreader.R
import com.zpr.rssreader.RssApplication
import kotlinx.coroutines.launch

/**
 * 订阅源列表状态与删除操作。
 */
class FeedListViewModel(application: Application) : AndroidViewModel(application) {

    private val feedRepository = (application as RssApplication)
        .container
        .feedRepository

    val feeds = feedRepository.observeFeedsWithUnread().asLiveData()

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    fun deleteFeed(feedId: Long) {
        viewModelScope.launch {
            feedRepository.deleteFeed(feedId)
            _message.value = getApplication<Application>()
                .getString(R.string.feed_deleted)
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
