package com.ledgerly.expense.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

/** Registers the periodic reminder jobs. Cadence is intentionally conservative. */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun scheduleAll() {
        schedule(ReminderType.MISSING_RECEIPTS, Duration.ofDays(7))
        schedule(ReminderType.MONTHLY_BOOKKEEPING, Duration.ofDays(30))
        schedule(ReminderType.QUARTERLY_TAXES, Duration.ofDays(90))
    }

    private fun schedule(type: ReminderType, interval: Duration) {
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(interval)
            .setInputData(Data.Builder().putString(ReminderWorker.KEY_TYPE, type.name).build())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "reminder_${type.name}",
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
