package com.example.rssreaderproject.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * RSS 文章数据访问接口（表：rss_article）
 */
@Dao
interface RssArticleDao {

    /** 新增文章；同一订阅源下链接重复时忽略（唯一索引保护） */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(article: RssArticle): Long

    /** 批量新增文章（拉取解析后整体入库） */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(articles: List<RssArticle>)

    /** 更新文章（如已读状态、正文内容） */
    @Update
    suspend fun update(article: RssArticle)

    /** 删除文章 */
    @Delete
    suspend fun delete(article: RssArticle)

    /** 按订阅源查询文章，按发布时间倒序 */
    @Query("SELECT * FROM rss_article WHERE sourceId = :sourceId ORDER BY pubDate DESC")
    fun getArticlesBySource(sourceId: Long): Flow<List<RssArticle>>

    /** 查询全部文章，按发布时间倒序 */
    @Query("SELECT * FROM rss_article ORDER BY pubDate DESC")
    fun getAllArticles(): Flow<List<RssArticle>>

    /** 统计某订阅源下文章数量 */
    @Query("SELECT COUNT(*) FROM rss_article WHERE sourceId = :sourceId")
    suspend fun countBySource(sourceId: Long): Int
}
