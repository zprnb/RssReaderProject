package com.zpr.rssreader.di

import android.content.Context
import com.zpr.rssreader.data.local.AppDatabase
import com.zpr.rssreader.data.remote.RssHttpClient
import com.zpr.rssreader.data.remote.RssParser
import com.zpr.rssreader.data.repository.RssFeedRepository
import com.zpr.rssreader.data.repository.RssItemRepository
import com.zpr.rssreader.data.repository.RssSyncRepository

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val database: AppDatabase = AppDatabase.getInstance(appContext)

    val feedRepository: RssFeedRepository = RssFeedRepository(database.rssFeedDao())

    val itemRepository: RssItemRepository = RssItemRepository(database.rssItemDao())

    val httpClient: RssHttpClient = RssHttpClient()

    val parser: RssParser = RssParser()

    val syncRepository: RssSyncRepository = RssSyncRepository(
        feedRepository = feedRepository,
        itemRepository = itemRepository,
        httpClient = httpClient,
        parser = parser
    )
}
