package com.zpr.rssreader.ui.favorite

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.zpr.rssreader.RssApplication

/**
 * 收藏列表状态。
 */
class FavoriteViewModel(application: Application) : AndroidViewModel(application) {

    val items = (application as RssApplication)
        .container
        .itemRepository
        .observeFavorites()
        .asLiveData()
}
