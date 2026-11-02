package com.zpr.rssreader.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

class RssHttpClient(
    private val client: OkHttpClient = createDefaultClient()
) {

    suspend fun fetch(rawUrl: String): String = withContext(Dispatchers.IO) {
        val url = normalizeUrl(rawUrl)
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept", ACCEPT)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw RssNetworkException("服务器返回 HTTP ${response.code}")
                }
                val body = response.body ?: throw RssNetworkException("响应内容为空")
                val bytes = body.bytes()
                if (bytes.isEmpty()) {
                    throw RssNetworkException("响应内容为空")
                }
                val charset = body.contentType()?.charset(Charsets.UTF_8)
                    ?: detectCharset(bytes)
                String(bytes, charset)
            }
        } catch (e: RssNetworkException) {
            throw e
        } catch (e: IllegalArgumentException) {
            throw RssNetworkException("RSS 地址格式不正确", e)
        } catch (e: IOException) {
            throw RssNetworkException("网络请求失败，请检查网络连接", e)
        }
    }

    fun normalizeUrl(rawUrl: String): String {
        val value = rawUrl.trim()
        if (value.isEmpty()) {
            throw RssNetworkException("RSS 地址不能为空")
        }
        val withScheme = if (
            value.startsWith("http://", ignoreCase = true) ||
            value.startsWith("https://", ignoreCase = true)
        ) {
            value
        } else {
            "https://$value"
        }
        val parsed = withScheme.toHttpUrlOrNull()
            ?: throw RssNetworkException("RSS 地址格式不正确")
        return parsed.toString()
    }

    private fun detectCharset(bytes: ByteArray): Charset {
        val head = String(
            bytes,
            0,
            minOf(bytes.size, CHARSET_SNIFF_LENGTH),
            Charsets.ISO_8859_1
        )
        val name = CHARSET_PATTERN.find(head)?.groupValues?.getOrNull(1)
        if (name.isNullOrBlank()) return Charsets.UTF_8
        return runCatching { Charset.forName(name) }.getOrDefault(Charsets.UTF_8)
    }

    companion object {
        private const val USER_AGENT = "ZprRssReader/1.0 (Android)"
        private const val ACCEPT =
            "application/rss+xml, application/atom+xml, application/xml, text/xml, */*"
        private const val CHARSET_SNIFF_LENGTH = 512
        private val CHARSET_PATTERN = Regex(
            "encoding\\s*=\\s*[\"']([^\"']+)[\"']",
            RegexOption.IGNORE_CASE
        )

        fun createDefaultClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .callTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .followSslRedirects(true)
                .build()
        }
    }
}
