package com.zpr.rssreader.ui.item

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.zpr.rssreader.data.local.entity.RssItem
import com.zpr.rssreader.RssApplication
import kotlinx.coroutines.launch

/**
 * 文章详情状态，负责自动标记已读与收藏切换。
 */
class ItemDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val itemRepository = (application as RssApplication)
        .container
        .itemRepository

    private val itemId = MutableLiveData<Long?>(null)
    private var markedRead = false

    val item = itemId.switchMap { id ->
        if (id == null || id <= 0L) {
            MutableLiveData<RssItem?>()
        } else {
            itemRepository.observeItem(id).asLiveData()
        }
    }

    fun setItemId(id: Long) {
        if (itemId.value != id) {
            itemId.value = id
            markedRead = false
        }
    }

    fun markReadOnce() {
        val id = itemId.value ?: return
        if (id <= 0L || markedRead) return

        markedRead = true
        viewModelScope.launch {
            itemRepository.setRead(id, true)
        }
    }

    fun toggleFavorite() {
        val id = itemId.value ?: return
        if (id <= 0L) return

        viewModelScope.launch {
            itemRepository.toggleFavorite(id)
        }
    }
}
