package com.zpr.rssreader.work

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.WorkInfo
import androidx.work.WorkManager
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class RefreshSchedulerTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val workManager = WorkManager.getInstance(context)

    @Test
    fun schedulesAndCancelsPeriodicRefresh() {
        RefreshScheduler.schedulePeriodic(context)

        val scheduled = workManager
            .getWorkInfosForUniqueWork(RefreshWorker.PERIODIC_WORK_NAME)
            .get(10, TimeUnit.SECONDS)

        assertTrue(scheduled.isNotEmpty())
        assertTrue(
            scheduled.any {
                it.state == WorkInfo.State.ENQUEUED ||
                    it.state == WorkInfo.State.RUNNING ||
                    it.state == WorkInfo.State.BLOCKED
            }
        )

        RefreshScheduler.cancelPeriodic(context)

        assertTrue(waitForCancelled())
    }

    private fun waitForCancelled(): Boolean {
        repeat(50) {
            val infos = workManager
                .getWorkInfosForUniqueWork(RefreshWorker.PERIODIC_WORK_NAME)
                .get(5, TimeUnit.SECONDS)
            if (infos.isNotEmpty() && infos.all { it.state == WorkInfo.State.CANCELLED }) {
                return true
            }
            Thread.sleep(200)
        }
        return false
    }
}
