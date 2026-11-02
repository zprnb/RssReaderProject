package com.zpr.rssreader.data.repository

import com.zpr.rssreader.data.local.entity.RssFeed
import com.zpr.rssreader.data.local.entity.RssItem
import com.zpr.rssreader.data.remote.RssHttpClient
import com.zpr.rssreader.data.remote.RssParser
import com.zpr.rssreader.data.remote.model.ParsedItem

class RssSyncRepository(
    private val feedRepository: RssFeedRepository,
    private val itemRepository: RssItemRepository,
    private val httpClient: RssHttpClient,
    private val parser: RssParser,
    private val clock: () -> Long = System::currentTimeMillis
) {

    suspend fun addFeed(url: String, customTitle: String? = null): AddFeedResult {
        val normalizedUrl = httpClient.normalizeUrl(url)
        val xml = httpClient.fetch(normalizedUrl)
        val parsed = parser.parse(xml)
        val now = clock()
        val existing = feedRepository.getFeedByUrl(normalizedUrl)

        val title = customTitle
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: existing?.title?.takeIf { it.isNotBlank() }
            ?: parsed.title

        val feed = RssFeed(
            id = existing?.id ?: 0L,
            title = title,
            url = normalizedUrl,
            siteUrl = parsed.link ?: existing?.siteUrl,
            description = parsed.description ?: existing?.description,
            lastFetchedAt = now,
            sortOrder = existing?.sortOrder ?: 0
        )

        val feedId = if (existing == null) {
            val newId = feedRepository.addFeed(feed)
            if (newId > 0L) {
                newId
            } else {
                feedRepository.getFeedByUrl(normalizedUrl)?.id
                    ?: throw RssSyncException("订阅源保存失败")
            }
        } else {
            feedRepository.updateFeed(feed)
            existing.id
        }

        val items = parsed.items.map { it.toEntity(feedId, now) }
        val inserted = itemRepository.saveItems(items)
        feedRepository.updateLastFetchedAt(feedId, now)

        return AddFeedResult(
            feedId = feedId,
            title = title,
            insertedCount = inserted,
            itemCount = parsed.items.size
        )
    }

    private fun ParsedItem.toEntity(feedId: Long, fetchedAt: Long): RssItem {
        return RssItem(
            feedId = feedId,
            guid = guid,
            title = title,
            link = link,
            author = author,
            summary = summary,
            content = content,
            pubDate = if (pubDate > 0L) pubDate else fetchedAt,
            fetchedAt = fetchedAt
        )
    }
}

data class AddFeedResult(
    val feedId: Long,
    val title: String,
    val insertedCount: Int,
    val itemCount: Int
)

class RssSyncException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
