package com.zpr.rssreader.data.remote

import com.zpr.rssreader.data.remote.model.ParsedFeed
import com.zpr.rssreader.data.remote.model.ParsedItem
import org.jdom2.Element
import org.jdom2.input.SAXBuilder
import org.xml.sax.InputSource
import java.io.StringReader
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

/**
 * RSS 2.0、Atom 与 RSS 1.0 的 XML 解析器。
 *
 * 使用 JDOM 将订阅内容转换为 [ParsedFeed]，并处理 CDATA、常见 HTML 实体、
 * 非法控制字符以及多种时间格式。
 */
class RssParser {

    /**
     * 解析完整 XML 文本；格式不受支持或 XML 非法时抛出 [RssParseException]。
     */
    fun parse(xml: String): ParsedFeed {
        val content = sanitize(xml)
        if (content.isBlank()) {
            throw RssParseException("订阅内容为空")
        }
        val document = try {
            createBuilder().build(StringReader(content))
        } catch (e: Exception) {
            throw RssParseException("XML 解析失败", e)
        }
        val root = try {
            document.rootElement
        } catch (e: Exception) {
            throw RssParseException("XML 缺少根节点", e)
        } ?: throw RssParseException("XML 缺少根节点")

        return when (root.name.lowercase(Locale.US)) {
            "rss" -> parseRss(root)
            "feed" -> parseAtom(root)
            "rdf" -> parseRss1(root)
            else -> throw RssParseException("不支持的订阅格式: ${root.name}")
        }
    }

    private fun parseRss(root: Element): ParsedFeed {
        val channel = root.firstChild("channel") ?: root
        val items = channel.children
            .filter { it.name.equals("item", true) }
            .mapNotNull { parseRssItem(it) }
            .sortedByDescending { it.pubDate }

        return ParsedFeed(
            title = channel.childText("title").orEmpty().toPlainText().ifBlank { "未命名订阅源" },
            link = channel.childText("link"),
            description = channel.childText("description", "subtitle"),
            items = items
        )
    }

    private fun parseRss1(root: Element): ParsedFeed {
        val channel = root.firstChild("channel")
        val items = root.children
            .filter { it.name.equals("item", true) }
            .mapNotNull { parseRssItem(it) }
            .sortedByDescending { it.pubDate }

        return ParsedFeed(
            title = channel?.childText("title").orEmpty().toPlainText().ifBlank { "未命名订阅源" },
            link = channel?.childText("link"),
            description = channel?.childText("description"),
            items = items
        )
    }

    private fun parseRssItem(item: Element): ParsedItem? {
        val title = item.childText("title").orEmpty().toPlainText()
        val link = item.childText("link")
        val summary = item.childText("description", "summary")
        val content = item.childText("encoded", "content") ?: summary
        val author = item.childText("author", "creator")?.toPlainText()
        val pubDate = parseDate(item.childText("pubDate", "date", "published", "updated"))

        if (title.isBlank() && link.isNullOrBlank() && content.isNullOrBlank()) {
            return null
        }

        val guid = item.childText("guid", "id") ?: link ?: fallbackGuid(title, pubDate)

        return ParsedItem(
            guid = guid,
            title = title.ifBlank { link ?: "无标题" },
            link = link,
            author = author,
            summary = summary,
            content = content,
            pubDate = pubDate
        )
    }

    private fun parseAtom(root: Element): ParsedFeed {
        val items = root.children
            .filter { it.name.equals("entry", true) }
            .mapNotNull { parseAtomEntry(it) }
            .sortedByDescending { it.pubDate }

        return ParsedFeed(
            title = root.childText("title").orEmpty().toPlainText().ifBlank { "未命名订阅源" },
            link = atomLink(root),
            description = root.childText("subtitle", "description"),
            items = items
        )
    }

    private fun parseAtomEntry(entry: Element): ParsedItem? {
        val title = entry.childText("title").orEmpty().toPlainText()
        val link = atomLink(entry)
        val summary = entry.childText("summary", "description")
        val content = entry.childText("content") ?: summary
        val author = entry.firstChild("author")?.childText("name")
            ?: entry.childText("author")?.toPlainText()
        val pubDate = parseDate(entry.childText("published", "updated", "modified"))

        if (title.isBlank() && link.isNullOrBlank() && content.isNullOrBlank()) {
            return null
        }

        val guid = entry.childText("id") ?: link ?: fallbackGuid(title, pubDate)

        return ParsedItem(
            guid = guid,
            title = title.ifBlank { link ?: "无标题" },
            link = link,
            author = author,
            summary = summary,
            content = content,
            pubDate = pubDate
        )
    }

    private fun atomLink(element: Element): String? {
        val links = element.children.filter { it.name.equals("link", true) }
        if (links.isEmpty()) return null

        val alternate = links.firstOrNull {
            val rel = it.getAttributeValue("rel")
            rel == null || rel.equals("alternate", true)
        } ?: links.first()

        return alternate.getAttributeValue("href")?.trim()?.takeIf { it.isNotEmpty() }
            ?: alternate.value.trim().takeIf { it.isNotEmpty() }
    }

    private fun Element.firstChild(vararg names: String): Element? {
        return children.firstOrNull { child ->
            names.any { name -> child.name.equals(name, true) }
        }
    }

    private fun Element.childText(vararg names: String): String? {
        return firstChild(*names)
            ?.value
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    private fun String.toPlainText(): String {
        return replace(HTML_TAG_PATTERN, " ")
            .replace('\u00A0', ' ')
            .replace(WHITESPACE_PATTERN, " ")
            .trim()
    }

    private fun fallbackGuid(title: String, pubDate: Long): String {
        return "fallback-${(title + pubDate.toString()).hashCode()}"
    }

    private fun sanitize(xml: String): String {
        val withoutBom = xml.removePrefix("\uFEFF").trim()
        val withoutControl = INVALID_XML_CHARS.replace(withoutBom, "")
        return HTML_ENTITY_PATTERN.replace(withoutControl) { match ->
            HTML_ENTITIES[match.groupValues[1].lowercase(Locale.US)] ?: match.value
        }
    }

    private fun createBuilder(): SAXBuilder {
        val builder = SAXBuilder()
        builder.setExpandEntities(false)
        builder.setEntityResolver { _, _ -> InputSource(StringReader("")) }
        return builder
    }

    private fun parseDate(raw: String?): Long {
        val value = raw?.trim().orEmpty()
        if (value.isEmpty()) return 0L

        runCatching {
            return OffsetDateTime.parse(value).toInstant().toEpochMilli()
        }
        runCatching {
            return ZonedDateTime.parse(value).toInstant().toEpochMilli()
        }
        runCatching {
            return ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME)
                .toInstant()
                .toEpochMilli()
        }
        runCatching {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }
        runCatching {
            return LocalDate.parse(value)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }

        for (pattern in LEGACY_DATE_PATTERNS) {
            runCatching {
                val format = SimpleDateFormat(pattern, Locale.US)
                format.timeZone = TimeZone.getTimeZone("UTC")
                val parsed = format.parse(value)
                if (parsed != null) return parsed.time
            }
        }
        return 0L
    }

    companion object {
        private val INVALID_XML_CHARS = Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]")
        private val HTML_ENTITY_PATTERN = Regex("&([a-zA-Z][a-zA-Z0-9]+);")
        private val HTML_TAG_PATTERN = Regex("<[^>]*>")
        private val WHITESPACE_PATTERN = Regex("\\s+")

        private val HTML_ENTITIES = mapOf(
            "nbsp" to "\u00A0",
            "ensp" to " ",
            "emsp" to " ",
            "mdash" to "\u2014",
            "ndash" to "\u2013",
            "hellip" to "\u2026",
            "lsquo" to "\u2018",
            "rsquo" to "\u2019",
            "ldquo" to "\u201C",
            "rdquo" to "\u201D",
            "middot" to "\u00B7",
            "bull" to "\u2022",
            "copy" to "\u00A9",
            "reg" to "\u00AE",
            "trade" to "\u2122",
            "times" to "\u00D7",
            "divide" to "\u00F7",
            "euro" to "\u20AC",
            "pound" to "\u00A3",
            "yen" to "\u00A5",
            "cent" to "\u00A2",
            "deg" to "\u00B0",
            "laquo" to "\u00AB",
            "raquo" to "\u00BB"
        )

        private val LEGACY_DATE_PATTERNS = listOf(
            "EEE, dd MMM yyyy HH:mm:ss zzz",
            "EEE, dd MMM yyyy HH:mm:ss Z",
            "EEE, dd MMM yyyy HH:mm Z",
            "dd MMM yyyy HH:mm:ss Z",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss"
        )
    }
}
