package com.zpr.rssreader.ui.feed

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zpr.rssreader.R
import com.zpr.rssreader.RssApplication
import com.zpr.rssreader.data.repository.AddFeedResult
import kotlinx.coroutines.launch

/**
 * 添加订阅源的加载状态、错误与成功事件。
 */
class AddFeedViewModel(application: Application) : AndroidViewModel(application) {

    private val syncRepository = (application as RssApplication)
        .container
        .syncRepository

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _success = MutableLiveData<AddFeedResult?>()
    val success: LiveData<AddFeedResult?> = _success

    fun addFeed(url: String, customTitle: String?) {
        if (_loading.value == true) return

        _loading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                _success.value = syncRepository.addFeed(url, customTitle)
            } catch (e: Exception) {
                _error.value = e.message
                    ?: getApplication<Application>().getString(R.string.add_feed_failed)
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccess() {
        _success.value = null
    }
}
