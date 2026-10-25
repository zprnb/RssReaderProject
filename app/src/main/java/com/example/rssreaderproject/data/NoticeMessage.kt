package com.example.rssreaderproject.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 通知消息实体（P3 通知提醒中心使用），对应表 notice_message
 * 记录系统推送过的每一条通知，供通知中心列表展示
 */
@Entity(tableName = "notice_message")
data class NoticeMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,                    // 通知标题
    val content: String,                   // 通知内容
    val articleId: Long = 0,               // 关联文章 id，0 表示无关联
    val createdAt: Long = System.currentTimeMillis(),  // 通知时间戳
    val read: Boolean = false              // 是否已读
)
