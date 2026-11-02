package com.zpr.reminder.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private const val BACKOFF_MINUTES = 10L

    fun schedulePeriodic(
        context: Context,
        intervalMinutes: Int = 60
    ) {
        val interval = intervalMinutes.coerceAtLeast(15).toLong()
        val request = PeriodicWorkRequestBuilder<ReminderCheckWorker>(
            interval,
            TimeUnit.MINUTES
        )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                BACKOFF_MINUTES,
                TimeUnit.MINUTES
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            ReminderCheckWorker.PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelPeriodic(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(
            ReminderCheckWorker.PERIODIC_WORK_NAME
        )
    }

    fun checkNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<ReminderCheckWorker>().build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            ReminderCheckWorker.ONE_TIME_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun scheduleSnooze(context: Context, delayMinutes: Int = 15) {
        val request = OneTimeWorkRequestBuilder<ReminderCheckWorker>()
            .setInitialDelay(delayMinutes.coerceAtLeast(1).toLong(), TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            ReminderCheckWorker.SNOOZE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
