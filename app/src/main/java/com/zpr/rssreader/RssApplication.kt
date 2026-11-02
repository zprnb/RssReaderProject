package com.zpr.rssreader

import android.app.Application
import com.zpr.rssreader.di.AppContainer
import com.zpr.rssreader.work.RefreshScheduler

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
