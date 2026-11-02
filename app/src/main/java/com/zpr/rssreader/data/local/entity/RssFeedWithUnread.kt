package com.zpr.rssreader.data.local.entity

import androidx.room.ColumnInfo

data class RssFeedWithUnread(
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "url")
    val url: String,
    @ColumnInfo(name = "site_url")
    val siteUrl: String?,
    @ColumnInfo(name = "description")
    val description: String?,
    @ColumnInfo(name = "last_fetched_at")
    val lastFetchedAt: Long,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
    @ColumnInfo(name = "unread_count")
    val unreadCount: Int
)
