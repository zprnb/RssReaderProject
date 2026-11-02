package com.zpr.rssreader.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rss_feeds",
    indices = [Index(value = ["url"], unique = true)]
)
data class RssFeed(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "url")
    val url: String,
    @ColumnInfo(name = "site_url")
    val siteUrl: String? = null,
    @ColumnInfo(name = "description")
    val description: String? = null,
    @ColumnInfo(name = "last_fetched_at")
    val lastFetchedAt: Long = 0L,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0
)
