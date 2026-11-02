package com.zpr.rssreader.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zpr.rssreader.data.local.dao.RssFeedDao
import com.zpr.rssreader.data.local.dao.RssItemDao
import com.zpr.rssreader.data.local.entity.RssFeed
import com.zpr.rssreader.data.local.entity.RssItem

@Database(
    entities = [RssFeed::class, RssItem::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun rssFeedDao(): RssFeedDao

    abstract fun rssItemDao(): RssItemDao

    companion object {
        private const val DATABASE_NAME = "rss_reader.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build().also { instance = it }
            }
        }
    }
}
