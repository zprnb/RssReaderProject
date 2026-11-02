package com.zpr.rssreader.data.remote.model

data class ParsedItem(
    val guid: String,
    val title: String,
    val link: String?,
    val author: String?,
    val summary: String?,
    val content: String?,
    val pubDate: Long
)
