package com.ledgerly.expense.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ledgerly.expense.core.AppResult
import com.ledgerly.expense.domain.repository.AuthRepository
import com.ledgerly.expense.domain.repository.RecurringExpenseRepository
import com.ledgerly.expense.domain.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Background worker that reconciles local data with Google Sheets/Drive. It is
 * enqueued on data changes (expedited) and periodically. WorkManager guarantees
 * execution once constraints (network) are met, giving us reliable offline-first
 * sync that resumes automatically when connectivity returns.
 */
@HiltWorker
class ExpenseSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncRepository: SyncRepository,
    private val recurringRepository: RecurringExpenseRepository,
    private val authRepository: AuthRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val userId = runCatching { authRepository.requireUserId() }.getOrNull()
            ?: return Result.success() // not signed in; nothing to sync

        // Generate any due recurring expenses before pushing.
        runCatching { recurringRepository.generateDue(userId) }

        return when (val result = syncRepository.syncNow(userId)) {
            is AppResult.Success -> Result.success()
            is AppResult.Error -> if (runAttemptCount < MAX_ATTEMPTS) Result.retry() else Result.failure()
        }
    }

    private companion object {
        const val MAX_ATTEMPTS = 5
    }
}
