package com.zpr.rssreader.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RssParserTest {

    private val parser = RssParser()

    @Test
    fun parsesRss2Feed() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0"
                xmlns:content="http://purl.org/rss/1.0/modules/content/"
                xmlns:dc="http://purl.org/dc/elements/1.1/">
                <channel>
                    <title>示例科技</title>
                    <link>https://example.com</link>
                    <description>科技资讯</description>
                    <item>
                        <title><![CDATA[Android 15 发布]]></title>
                        <link>https://example.com/a/1</link>
                        <guid>post-1</guid>
                        <description><![CDATA[<p>系统更新</p>]]></description>
                        <content:encoded><![CDATA[<p>完整内容</p>]]></content:encoded>
                        <dc:creator>张三</dc:creator>
                        <pubDate>Mon, 02 Nov 2026 08:30:00 GMT</pubDate>
                    </item>
                    <item>
                        <title>第二条</title>
                        <link>https://example.com/a/2</link>
                        <pubDate>Sun, 01 Nov 2026 09:00:00 GMT</pubDate>
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        val feed = parser.parse(xml)

        assertEquals("示例科技", feed.title)
        assertEquals("https://example.com", feed.link)
        assertEquals(2, feed.items.size)
        val first = feed.items.first()
        assertEquals("Android 15 发布", first.title)
        assertEquals("https://example.com/a/1", first.link)
        assertEquals("post-1", first.guid)
        assertEquals("张三", first.author)
        assertTrue(first.content.orEmpty().contains("完整内容"))
        assertTrue(first.pubDate > 0L)
    }

    @Test
    fun parsesAtomFeed() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <feed xmlns="http://www.w3.org/2005/Atom">
                <title>Atom 示例</title>
                <link href="https://example.org"/>
                <subtitle>示例订阅</subtitle>
                <entry>
                    <title>第一条</title>
                    <link rel="alternate" href="https://example.org/1"/>
                    <id>urn:uuid:1</id>
                    <updated>2026-11-01T10:00:00Z</updated>
                    <summary>摘要</summary>
                    <content type="html">&lt;p&gt;正文&lt;/p&gt;</content>
                    <author><name>李四</name></author>
                </entry>
            </feed>
        """.trimIndent()

        val feed = parser.parse(xml)

        assertEquals("Atom 示例", feed.title)
        assertEquals("https://example.org", feed.link)
        assertEquals(1, feed.items.size)
        val entry = feed.items.first()
        assertEquals("第一条", entry.title)
        assertEquals("https://example.org/1", entry.link)
        assertEquals("urn:uuid:1", entry.guid)
        assertEquals("李四", entry.author)
        assertEquals("<p>正文</p>", entry.content)
        assertTrue(entry.pubDate > 0L)
    }

    @Test
    fun decodesNamedHtmlEntitiesInTitle() {
        val xml = """
            <rss version="2.0">
                <channel>
                    <title>科技&nbsp;资讯</title>
                    <item>
                        <title>A&amp;B&nbsp;C</title>
                        <link>https://example.com/1</link>
                        <pubDate>Mon, 02 Nov 2026 08:30:00 GMT</pubDate>
                    </item>
                </channel>
            </rss>
        """.trimIndent()

        val feed = parser.parse(xml)

        assertEquals("科技 资讯", feed.title)
        assertEquals("A&B C", feed.items.first().title)
    }

    @Test(expected = RssParseException::class)
    fun rejectsUnsupportedFormat() {
        parser.parse("<html><body>not a feed</body></html>")
    }
}
