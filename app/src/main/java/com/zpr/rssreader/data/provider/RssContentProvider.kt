package com.zpr.rssreader.data.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.zpr.rssreader.data.local.AppDatabase

/**
 * 向伴生通知模块暴露未读文章数据。
 *
 * 查询参数：since（按抓取时间过滤）、limit（返回条数上限）。
 * 同时支持通过 update 将指定文章标记为已读。
 */
class RssContentProvider : ContentProvider() {

    private val database
        get() = AppDatabase.getInstance(requireNotNull(context).applicationContext)

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        if (uriMatcher.match(uri) != CODE_ARTICLES) return null

        val since = uri.getQueryParameter(PARAM_SINCE)?.toLongOrNull() ?: 0L
        val limit = (uri.getQueryParameter(PARAM_LIMIT)?.toIntOrNull() ?: DEFAULT_LIMIT)
            .coerceIn(1, MAX_LIMIT)

        return database.openHelper.readableDatabase.query(
            ARTICLES_SQL,
            arrayOf<Any?>(since, limit)
        )
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        if (uriMatcher.match(uri) != CODE_ARTICLES || values == null) return 0

        val count = database.openHelper.writableDatabase.update(
            TABLE_ITEMS,
            SQLiteDatabase.CONFLICT_NONE,
            values,
            selection,
            selectionArgs
        )
        if (count > 0) {
            context?.contentResolver?.notifyChange(uri, null)
        }
        return count
    }

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    companion object {
        const val AUTHORITY = "com.zpr.rssreader.provider"
        const val PATH_ARTICLES = "articles"
        const val PARAM_SINCE = "since"
        const val PARAM_LIMIT = "limit"

        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$PATH_ARTICLES")

        private const val CODE_ARTICLES = 1
        private const val DEFAULT_LIMIT = 20
        private const val MAX_LIMIT = 100
        private const val TABLE_ITEMS = "rss_items"

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY, PATH_ARTICLES, CODE_ARTICLES)
        }

        private val ARTICLES_SQL = """
            SELECT i.id,
                   i.title,
                   f.title AS feed_title,
                   i.link,
                   i.pub_date,
                   i.fetched_at
            FROM rss_items i
            INNER JOIN rss_feeds f ON f.id = i.feed_id
            WHERE i.is_read = 0 AND i.fetched_at > ?
            ORDER BY i.pub_date DESC, i.id DESC
            LIMIT ?
        """.trimIndent()
    }
}
