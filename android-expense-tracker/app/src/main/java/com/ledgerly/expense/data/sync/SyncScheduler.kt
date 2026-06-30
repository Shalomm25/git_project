package com.ledgerly.expense.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules sync work. A one-off expedited job runs after each change; a
 * periodic job acts as a safety net to catch anything missed (e.g. failures or
 * edits made while offline).
 */
@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val workManager get() = WorkManager.getInstance(context)

    /** Request an immediate sync, coalescing rapid successive requests. */
    fun requestSync(requireUnmetered: Boolean = false) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (requireUnmetered) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<ExpenseSyncWorker>()
            .setConstraints(constraints)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, Duration.ofSeconds(30))
            .build()
        workManager.enqueueUniqueWork(UNIQUE_ONE_OFF, ExistingWorkPolicy.REPLACE, request)
    }

    /** Register the periodic safety-net sync (call once at startup). */
    fun schedulePeriodicSync(requireUnmetered: Boolean = false) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (requireUnmetered) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<ExpenseSyncWorker>(Duration.ofHours(6))
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, Duration.ofMinutes(5))
            .build()
        workManager.enqueueUniquePeriodicWork(
            UNIQUE_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun cancelAll() {
        workManager.cancelUniqueWork(UNIQUE_ONE_OFF)
        workManager.cancelUniqueWork(UNIQUE_PERIODIC)
    }

    private companion object {
        const val UNIQUE_ONE_OFF = "ledgerly_sync_now"
        const val UNIQUE_PERIODIC = "ledgerly_sync_periodic"
    }
}
