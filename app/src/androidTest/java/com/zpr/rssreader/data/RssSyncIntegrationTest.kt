package com.zpr.rssreader.data

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zpr.rssreader.RssApplication
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.InetAddress
import java.net.ServerSocket

@RunWith(AndroidJUnit4::class)
class RssSyncIntegrationTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    private lateinit var serverSocket: ServerSocket
    private var port = 0

    @Before
    fun startServer() {
        serverSocket = ServerSocket(0, 4, InetAddress.getByName("127.0.0.1"))
        port = serverSocket.localPort

        val thread = Thread {
            try {
                while (!serverSocket.isClosed) {
                    serverSocket.accept().use { socket ->
                        val reader = socket.getInputStream().bufferedReader()
                        val requestLine = reader.readLine().orEmpty()
                        while (true) {
                            val line = reader.readLine() ?: break
                            if (line.isEmpty()) break
                        }

                        val path = requestLine.split(" ").getOrNull(1).orEmpty()
                        val success = path.startsWith("/feed")
                        val body = if (success) RSS_XML else ""
                        val status = if (success) "200 OK" else "404 Not Found"
                        val bytes = body.toByteArray(Charsets.UTF_8)
                        val header = buildString {
                            append("HTTP/1.1 $status\r\n")
                            append("Content-Type: application/rss+xml; charset=utf-8\r\n")
                            append("Content-Length: ${bytes.size}\r\n")
                            append("Connection: close\r\n\r\n")
                        }.toByteArray(Charsets.UTF_8)

                        socket.getOutputStream().write(header)
                        socket.getOutputStream().write(bytes)
                        socket.getOutputStream().flush()
                    }
                }
            } catch (_: Exception) {
            }
        }
        thread.isDaemon = true
        thread.start()
    }

    @After
    fun stopServer() {
        serverSocket.close()
    }

    @Test
    fun syncsRemoteFeedAndExposesArticlesThroughProvider() {
        runBlocking {
            val application = context.applicationContext as RssApplication
            val unique = System.currentTimeMillis()
            val feedTitle = "集成测试订阅-$unique"
            val feedUrl = "http://127.0.0.1:$port/feed/$unique"

            val result = application.container.syncRepository.addFeed(feedUrl, feedTitle)

            assertEquals(2, result.itemCount)
            assertEquals(2, result.insertedCount)
            assertNotNull(application.container.feedRepository.getFeed(result.feedId))

            val cursor = context.contentResolver.query(
                Uri.parse(
                    "content://com.zpr.rssreader.provider/articles?since=0&limit=50"
                ),
                null,
                null,
                null,
                null
            )
            assertNotNull(cursor)

            var matched = 0
            cursor!!.use {
                val feedTitleIndex = it.getColumnIndexOrThrow("feed_title")
                while (it.moveToNext()) {
                    if (it.getString(feedTitleIndex) == feedTitle) {
                        matched++
                    }
                }
            }
            assertEquals(2, matched)
        }
    }

    companion object {
        private val RSS_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
                <channel>
                    <title>集成测试订阅</title>
                    <link>https://example.com</link>
                    <description>联调测试</description>
                    <item>
                        <title>联调测试文章一</title>
                        <link>https://example.com/1</link>
                        <guid>integration-1</guid>
                        <description>用于验证主 App 与伴生模块联调的第一篇文章</description>
                        <pubDate>Mon, 02 Nov 2026 08:30:00 GMT</pubDate>
                    </item>
                    <item>
                        <title>联调测试文章二</title>
                        <link>https://example.com/2</link>
                        <guid>integration-2</guid>
                        <description>用于验证主 App 与伴生模块联调的第二篇文章</description>
                        <pubDate>Mon, 02 Nov 2026 09:00:00 GMT</pubDate>
                    </item>
                </channel>
            </rss>
        """.trimIndent()
    }
}
