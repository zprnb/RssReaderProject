package com.example.rssreaderproject.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 关键词过滤规则实体（关键词内容过滤功能），对应表 filter_rule
 * 命中关键词的 RSS 文章将被过滤，不展示
 */
@Entity(tableName = "filter_rule")
data class FilterRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val keyword: String,               // 过滤关键词
    val enabled: Boolean = true,       // 是否启用
    val createdAt: Long = System.currentTimeMillis()  // 创建时间戳
)
