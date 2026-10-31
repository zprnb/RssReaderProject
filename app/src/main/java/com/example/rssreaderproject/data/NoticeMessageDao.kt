package com.example.rssreaderproject.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 通知消息数据访问接口（表：notice_message，供 P3 通知提醒中心使用）
 */
@Dao
interface NoticeMessageDao {

    /** 新增一条通知记录 */
    @Insert
    suspend fun insert(message: NoticeMessage): Long

    /** 查询全部通知，按时间倒序 */
    @Query("SELECT * FROM notice_message ORDER BY createdAt DESC")
    fun getAllMessages(): Flow<List<NoticeMessage>>

    /** 统计未读通知数量 */
    @Query("SELECT COUNT(*) FROM notice_message WHERE read = 0")
    suspend fun countUnread(): Int

    /** 标记单条通知为已读 */
    @Query("UPDATE notice_message SET read = 1 WHERE id = :id")
    suspend fun markRead(id: Long)

    /** 清空全部通知记录 */
    @Query("DELETE FROM notice_message")
    suspend fun clearAll()
}
