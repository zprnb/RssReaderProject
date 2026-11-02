package com.zpr.rssreader.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zpr.rssreader.data.local.entity.RssFeed
import com.zpr.rssreader.data.local.entity.RssFeedWithUnread
import kotlinx.coroutines.flow.Flow

@Dao
interface RssFeedDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(feed: RssFeed): Long

    @Update
    suspend fun update(feed: RssFeed)

    @Delete
    suspend fun delete(feed: RssFeed)

    @Query("DELETE FROM rss_feeds WHERE id = :feedId")
    suspend fun deleteById(feedId: Long)

    @Query("SELECT * FROM rss_feeds ORDER BY sort_order ASC, title ASC")
    fun observeAll(): Flow<List<RssFeed>>

    @Query("SELECT * FROM rss_feeds ORDER BY sort_order ASC, title ASC")
    suspend fun getAll(): List<RssFeed>

    @Query("SELECT * FROM rss_feeds WHERE id = :feedId LIMIT 1")
    suspend fun getById(feedId: Long): RssFeed?

    @Query("SELECT * FROM rss_feeds WHERE url = :url LIMIT 1")
    suspend fun getByUrl(url: String): RssFeed?

    @Query("UPDATE rss_feeds SET last_fetched_at = :time WHERE id = :feedId")
    suspend fun updateLastFetchedAt(feedId: Long, time: Long)

    @Query("SELECT COUNT(*) FROM rss_feeds")
    suspend fun count(): Int

    @Query(
        """
        SELECT f.id,
               f.title,
               f.url,
               f.site_url,
               f.description,
               f.last_fetched_at,
               f.sort_order,
               (SELECT COUNT(*) FROM rss_items i WHERE i.feed_id = f.id AND i.is_read = 0) AS unread_count
        FROM rss_feeds f
        ORDER BY f.sort_order ASC, f.title ASC
        """
    )
    fun observeAllWithUnread(): Flow<List<RssFeedWithUnread>>
}
