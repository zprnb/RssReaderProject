package com.zpr.reminder.data

data class RemoteArticle(
    val id: Long,
    val title: String,
    val feedTitle: String,
    val link: String?,
    val pubDate: Long,
    val fetchedAt: Long
)
