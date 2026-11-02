package com.zpr.rssreader.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun formatPubDate(timestamp: Long): String {
        if (timestamp <= 0L) return ""
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .format(formatter)
    }
}
