package com.zpr.rssreader.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zpr.rssreader.data.local.entity.RssItem
import kotlinx.coroutines.flow.Flow

@Dao
interface RssItemDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<RssItem>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RssItem): Long

    @Query("SELECT * FROM rss_items WHERE id = :itemId LIMIT 1")
    suspend fun getById(itemId: Long): RssItem?

    @Query("SELECT * FROM rss_items WHERE id = :itemId LIMIT 1")
    fun observeById(itemId: Long): Flow<RssItem?>

    @Query("SELECT * FROM rss_items ORDER BY pub_date DESC, id DESC")
    fun observeAll(): Flow<List<RssItem>>

    @Query("SELECT * FROM rss_items WHERE feed_id = :feedId ORDER BY pub_date DESC, id DESC")
    fun observeByFeed(feedId: Long): Flow<List<RssItem>>

    @Query("SELECT * FROM rss_items WHERE is_favorite = 1 ORDER BY pub_date DESC, id DESC")
    fun observeFavorites(): Flow<List<RssItem>>

    @Query("SELECT * FROM rss_items WHERE feed_id = :feedId ORDER BY pub_date DESC, id DESC")
    suspend fun getByFeed(feedId: Long): List<RssItem>

    @Query("UPDATE rss_items SET is_read = :read WHERE id = :itemId")
    suspend fun setRead(itemId: Long, read: Boolean)

    @Query("UPDATE rss_items SET is_read = 1 WHERE feed_id = :feedId")
    suspend fun markAllReadByFeed(feedId: Long)

    @Query("UPDATE rss_items SET is_read = 1")
    suspend fun markAllRead()

    @Query("UPDATE rss_items SET is_favorite = :favorite WHERE id = :itemId")
    suspend fun setFavorite(itemId: Long, favorite: Boolean)

    @Query("DELETE FROM rss_items WHERE feed_id = :feedId")
    suspend fun deleteByFeed(feedId: Long)

    @Query("DELETE FROM rss_items WHERE is_read = 1 AND is_favorite = 0")
    suspend fun deleteReadItems(): Int

    @Query("SELECT COUNT(*) FROM rss_items")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM rss_items WHERE is_read = 0")
    fun observeUnreadCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM rss_items WHERE is_read = 0 AND feed_id = :feedId")
    fun observeUnreadCountByFeed(feedId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM rss_items WHERE is_read = 0 AND fetched_at > :since")
    suspend fun countUnreadSince(since: Long): Int

    @Query(
        """
        SELECT * FROM rss_items
        WHERE is_read = 0 AND fetched_at > :since
        ORDER BY pub_date DESC, id DESC
        LIMIT :limit
        """
    )
    suspend fun getUnreadSince(since: Long, limit: Int): List<RssItem>

    @Query("SELECT MAX(pub_date) FROM rss_items")
    suspend fun latestPubDate(): Long?
}
