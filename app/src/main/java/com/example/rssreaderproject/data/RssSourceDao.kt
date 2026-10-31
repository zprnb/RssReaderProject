package com.example.rssreaderproject.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * 订阅源数据访问接口（表：rss_source）
 */
@Dao
interface RssSourceDao {

    /** 新增订阅源，返回自增主键 */
    @Insert
    suspend fun insert(source: RssSource): Long

    /** 更新订阅源（如修改分组、名称） */
    @Update
    suspend fun update(source: RssSource)

    /** 删除订阅源（级联删除其下文章） */
    @Delete
    suspend fun delete(source: RssSource)

    /** 查询全部订阅源，按添加时间倒序 */
    @Query("SELECT * FROM rss_source ORDER BY createdAt DESC")
    fun getAllSources(): Flow<List<RssSource>>

    /** 按分组查询订阅源（订阅分组管理功能） */
    @Query("SELECT * FROM rss_source WHERE groupName = :groupName ORDER BY createdAt DESC")
    fun getSourcesByGroup(groupName: String): Flow<List<RssSource>>

    /** 查询所有分组名（去重，用于分组列表展示） */
    @Query("SELECT DISTINCT groupName FROM rss_source")
    fun getAllGroups(): Flow<List<String>>

    /** 按 id 查询单个订阅源 */
    @Query("SELECT * FROM rss_source WHERE id = :id")
    suspend fun getSourceById(id: Long): RssSource?
}
