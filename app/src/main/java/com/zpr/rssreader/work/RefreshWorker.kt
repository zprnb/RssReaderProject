package com.zpr.rssreader.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zpr.rssreader.RssApplication
import kotlinx.coroutines.CancellationException

/**
 * 主 App 后台刷新任务。
 *
 * 在 WorkManager 调度下刷新全部订阅源；全部失败时重试，部分成功时保留结果。
 */
class RefreshWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val application = applicationContext as? RssApplication
            ?: return Result.failure()

        return try {
            val summary = application.container.syncRepository.refreshAllFeeds()
            if (summary.total > 0 && summary.success == 0) {
                Result.retry()
            } else {
                Result.success()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val PERIODIC_WORK_NAME = "rss_periodic_refresh"
        const val ONE_TIME_WORK_NAME = "rss_refresh_now"
    }
}
