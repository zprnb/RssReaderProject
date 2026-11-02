package com.zpr.reminder.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri

class RssArticleClient(private val context: Context) {

    fun queryNewArticles(since: Long, limit: Int = DEFAULT_LIMIT): List<RemoteArticle>? {
        val uri = Uri.parse(CONTENT_URI)
            .buildUpon()
            .appendQueryParameter(PARAM_SINCE, since.toString())
            .appendQueryParameter(PARAM_LIMIT, limit.toString())
            .build()

        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow("id")
                val titleIndex = cursor.getColumnIndexOrThrow("title")
                val feedTitleIndex = cursor.getColumnIndexOrThrow("feed_title")
                val linkIndex = cursor.getColumnIndexOrThrow("link")
                val pubDateIndex = cursor.getColumnIndexOrThrow("pub_date")
                val fetchedAtIndex = cursor.getColumnIndexOrThrow("fetched_at")

                buildList {
                    while (cursor.moveToNext()) {
                        add(
                            RemoteArticle(
                                id = cursor.getLong(idIndex),
                                title = cursor.getString(titleIndex) ?: "",
                                feedTitle = cursor.getString(feedTitleIndex) ?: "",
                                link = cursor.getString(linkIndex),
                                pubDate = cursor.getLong(pubDateIndex),
                                fetchedAt = cursor.getLong(fetchedAtIndex)
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun markArticlesRead(ids: List<Long>): Int {
        if (ids.isEmpty()) return 0

        val selection = "id IN (${ids.joinToString(",") { "?" }})"
        val values = ContentValues().apply {
            put("is_read", 1)
        }
        val args = ids.map { it.toString() }.toTypedArray()

        return try {
            context.contentResolver.update(
                Uri.parse(CONTENT_URI),
                values,
                selection,
                args
            )
        } catch (e: Exception) {
            0
        }
    }

    companion object {
        private const val CONTENT_URI = "content://com.zpr.rssreader.provider/articles"
        private const val PARAM_SINCE = "since"
        private const val PARAM_LIMIT = "limit"
        private const val DEFAULT_LIMIT = 20
    }
}
