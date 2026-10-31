package com.example.rssreaderproject.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * 应用数据库：统一管理全部实体与 DAO
 * 版本 1：订阅源 / 文章 / 通知消息 / 关键词过滤规则
 */
@Database(
    entities = [
        RssSource::class,
        RssArticle::class,
        NoticeMessage::class,
        FilterRule::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun rssSourceDao(): RssSourceDao

    abstract fun rssArticleDao(): RssArticleDao

    abstract fun noticeMessageDao(): NoticeMessageDao

    abstract fun filterRuleDao(): FilterRuleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * 获取数据库单例（双重校验锁，保证线程安全）
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rss_reader.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
