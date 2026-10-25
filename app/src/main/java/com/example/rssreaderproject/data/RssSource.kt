package com.example.rssreaderproject.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * RSS订阅源实体，对应表 rss_source
 */
@Entity(tableName = "rss_source")
data class RssSource(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,   // 自增主键
    val name: String,                                    // 订阅源名称
    val url: String,                                     // RSS 订阅地址
    val groupName: String = "默认分组",                  // 订阅分组（订阅分组管理功能）
    val createdAt: Long = System.currentTimeMillis()     // 添加时间戳
)
