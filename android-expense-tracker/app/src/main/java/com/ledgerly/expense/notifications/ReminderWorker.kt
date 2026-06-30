package com.ledgerly.expense.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ledgerly.expense.domain.repository.AuthRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Periodic worker that surfaces bookkeeping reminders. The [ReminderType] is
 * passed via input data so a single worker class backs all reminder kinds.
 */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val notificationHelper: NotificationHelper,
    private val authRepository: AuthRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        runCatching { authRepository.requireUserId() }.getOrNull() ?: return Result.success()
        val type = inputData.getString(KEY_TYPE)?.let {
            runCatching { ReminderType.valueOf(it) }.getOrNull()
        } ?: return Result.success()

        notificationHelper.notifyReminder(type.notificationId, type.title, type.body)
        return Result.success()
    }

    companion object {
        const val KEY_TYPE = "reminder_type"
    }
}

/** The kinds of reminders Ledgerly can schedule. */
enum class ReminderType(val notificationId: Int, val title: String, val body: String) {
    MISSING_RECEIPTS(101, "Add missing receipts", "Some recent expenses are missing receipts. Tap to attach them before tax time."),
    QUARTERLY_TAXES(102, "Quarterly estimated taxes", "An estimated tax payment deadline is coming up. Review your numbers."),
    MONTHLY_BOOKKEEPING(103, "Monthly bookkeeping", "Take a few minutes to review and categorize this month's expenses."),
    RECURRING_DUE(104, "Recurring expense due", "A recurring expense is due. We've added it — review and attach a receipt."),
}
