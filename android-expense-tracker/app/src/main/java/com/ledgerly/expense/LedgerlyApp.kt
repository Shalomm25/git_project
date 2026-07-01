package com.ledgerly.expense

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.ledgerly.expense.data.sync.SyncScheduler
import com.ledgerly.expense.notifications.NotificationHelper
import com.ledgerly.expense.notifications.ReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application entry point. Wires up Hilt, the WorkManager [HiltWorkerFactory]
 * (so workers can use constructor injection), notification channels, and the
 * periodic sync/reminder jobs.
 */
@HiltAndroidApp
class LedgerlyApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var syncScheduler: SyncScheduler
    @Inject lateinit var reminderScheduler: ReminderScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createChannels()
        syncScheduler.schedulePeriodicSync()
        reminderScheduler.scheduleAll()
    }
}
