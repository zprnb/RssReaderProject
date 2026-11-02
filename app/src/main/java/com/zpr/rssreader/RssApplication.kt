package com.zpr.rssreader

import android.app.Application
import com.zpr.rssreader.di.AppContainer
import com.zpr.rssreader.work.RefreshScheduler

/**
 * 主 App 应用入口：初始化依赖容器，并按用户设置注册后台刷新任务。
 */
class RssApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        if (container.preferences.autoRefreshEnabled) {
            RefreshScheduler.schedulePeriodic(this)
        }
    }
}
