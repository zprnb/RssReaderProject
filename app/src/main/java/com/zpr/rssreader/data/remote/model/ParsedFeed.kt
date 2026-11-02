package com.zpr.rssreader.data.remote.model

data class ParsedFeed(
    val title: String,
    val link: String?,
    val description: String?,
    val items: List<ParsedItem>
)
