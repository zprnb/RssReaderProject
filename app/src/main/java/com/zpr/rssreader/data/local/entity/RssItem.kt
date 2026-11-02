package com.zpr.rssreader.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rss_items",
    foreignKeys = [
        ForeignKey(
            entity = RssFeed::class,
            parentColumns = ["id"],
            childColumns = ["feed_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["feed_id"]),
        Index(value = ["feed_id", "guid"], unique = true),
        Index(value = ["pub_date"]),
        Index(value = ["is_read"]),
        Index(value = ["is_favorite"]),
        Index(value = ["fetched_at"])
    ]
)
data class RssItem(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,
    @ColumnInfo(name = "feed_id")
    val feedId: Long,
    @ColumnInfo(name = "guid")
    val guid: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "link")
    val link: String? = null,
    @ColumnInfo(name = "author")
    val author: String? = null,
    @ColumnInfo(name = "summary")
    val summary: String? = null,
    @ColumnInfo(name = "content")
    val content: String? = null,
    @ColumnInfo(name = "pub_date")
    val pubDate: Long = 0L,
    @ColumnInfo(name = "fetched_at")
    val fetchedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false
)
