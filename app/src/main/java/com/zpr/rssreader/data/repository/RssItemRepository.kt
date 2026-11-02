package com.zpr.rssreader.data.repository

import com.zpr.rssreader.data.local.dao.RssItemDao
import com.zpr.rssreader.data.local.entity.RssItem
import kotlinx.coroutines.flow.Flow

class RssItemRepository(
    private val itemDao: RssItemDao
) {

    fun observeAllItems(): Flow<List<RssItem>> = itemDao.observeAll()

    fun observeItemsByFeed(feedId: Long): Flow<List<RssItem>> = itemDao.observeByFeed(feedId)

    fun observeFavorites(): Flow<List<RssItem>> = itemDao.observeFavorites()

    fun observeItem(itemId: Long): Flow<RssItem?> = itemDao.observeById(itemId)

    fun observeUnreadCount(): Flow<Int> = itemDao.observeUnreadCount()

    fun observeUnreadCountByFeed(feedId: Long): Flow<Int> = itemDao.observeUnreadCountByFeed(feedId)

    suspend fun getItem(itemId: Long): RssItem? = itemDao.getById(itemId)

    suspend fun getItemsByFeed(feedId: Long): List<RssItem> = itemDao.getByFeed(feedId)

    suspend fun saveItems(items: List<RssItem>): Int {
        if (items.isEmpty()) return 0
        return itemDao.insertAll(items).count { it != -1L }
    }

    suspend fun saveItem(item: RssItem): Long = itemDao.insert(item)

    suspend fun setRead(itemId: Long, read: Boolean) = itemDao.setRead(itemId, read)

    suspend fun markAllRead(feedId: Long? = null) {
        if (feedId == null) {
            itemDao.markAllRead()
        } else {
            itemDao.markAllReadByFeed(feedId)
        }
    }

    suspend fun setFavorite(itemId: Long, favorite: Boolean) = itemDao.setFavorite(itemId, favorite)

    suspend fun toggleFavorite(itemId: Long): Boolean {
        val item = itemDao.getById(itemId) ?: return false
        val next = !item.isFavorite
        itemDao.setFavorite(itemId, next)
        return next
    }

    suspend fun deleteItemsByFeed(feedId: Long) = itemDao.deleteByFeed(feedId)

    suspend fun deleteReadItems(): Int = itemDao.deleteReadItems()

    suspend fun countItems(): Int = itemDao.count()

    suspend fun countUnreadSince(since: Long): Int = itemDao.countUnreadSince(since)

    suspend fun getUnreadSince(since: Long, limit: Int = 20): List<RssItem> =
        itemDao.getUnreadSince(since, limit)

    suspend fun latestPubDate(): Long? = itemDao.latestPubDate()
}
