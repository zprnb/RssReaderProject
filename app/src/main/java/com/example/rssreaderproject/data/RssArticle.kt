package com.example.rssreaderproject.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * RSS 文章实体，对应表 rss_article
 * 每篇文章属于一个订阅源，订阅源删除时级联删除其文章
 */
@Entity(
    tableName = "rss_article",
    foreignKeys = [ForeignKey(
        entity = RssSource::class,
        parentColumns = ["id"],
        childColumns = ["sourceId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["sourceId", "link"], unique = true)] // 同一订阅源下链接不重复
)
data class RssArticle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceId: Long,              // 所属订阅源 id
    val title: String,                // 文章标题
    val link: String,                 // 文章链接
    val description: String,          // 摘要内容
    val content: String = "",         // 文章正文（无广告阅读模式用）
    val pubDate: Long,                // 发布时间戳
    val read: Boolean = false         // 是否已读
)
