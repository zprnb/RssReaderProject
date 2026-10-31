package com.example.rssreaderproject.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * 关键词过滤规则数据访问接口（表：filter_rule，关键词内容过滤功能）
 */
@Dao
interface FilterRuleDao {

    /** 新增过滤规则 */
    @Insert
    suspend fun insert(rule: FilterRule): Long

    /** 更新过滤规则（如启停、修改关键词） */
    @Update
    suspend fun update(rule: FilterRule)

    /** 删除过滤规则 */
    @Delete
    suspend fun delete(rule: FilterRule)

    /** 查询全部启用中的规则（内容过滤时使用） */
    @Query("SELECT * FROM filter_rule WHERE enabled = 1 ORDER BY createdAt DESC")
    fun getEnabledRules(): Flow<List<FilterRule>>

    /** 查询全部规则（管理页面使用） */
    @Query("SELECT * FROM filter_rule ORDER BY createdAt DESC")
    fun getAllRules(): Flow<List<FilterRule>>
}
