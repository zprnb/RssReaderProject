package com.zpr.rssreader.data.repository

import com.zpr.rssreader.data.local.dao.RssFeedDao
import com.zpr.rssreader.data.local.entity.RssFeed
import com.zpr.rssreader.data.local.entity.RssFeedWithUnread
import kotlinx.coroutines.flow.Flow

class RssFeedRepository(
    private val feedDao: RssFeedDao
) {

    fun observeFeeds(): Flow<List<RssFeed>> = feedDao.observeAll()

    fun observeFeedsWithUnread(): Flow<List<RssFeedWithUnread>> = feedDao.observeAllWithUnread()

    suspend fun getAllFeeds(): List<RssFeed> = feedDao.getAll()

    suspend fun getFeed(feedId: Long): RssFeed? = feedDao.getById(feedId)

    suspend fun getFeedByUrl(url: String): RssFeed? = feedDao.getByUrl(url)

    suspend fun addFeed(feed: RssFeed): Long = feedDao.insert(feed)

    suspend fun updateFeed(feed: RssFeed) = feedDao.update(feed)

    suspend fun deleteFeed(feedId: Long) = feedDao.deleteById(feedId)

    suspend fun updateLastFetchedAt(feedId: Long, time: Long = System.currentTimeMillis()) {
        feedDao.updateLastFetchedAt(feedId, time)
    }

    suspend fun countFeeds(): Int = feedDao.count()
}
