package com.zpr.rssreader.di

import android.content.Context
import com.zpr.rssreader.data.local.AppDatabase
import com.zpr.rssreader.data.repository.RssFeedRepository
import com.zpr.rssreader.data.repository.RssItemRepository

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val database: AppDatabase = AppDatabase.getInstance(appContext)

    val feedRepository: RssFeedRepository = RssFeedRepository(database.rssFeedDao())

    val itemRepository: RssItemRepository = RssItemRepository(database.rssItemDao())
}
