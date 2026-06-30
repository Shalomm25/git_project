package com.ledgerly.expense.domain.usecase

import com.ledgerly.expense.core.AppResult
import com.ledgerly.expense.core.util.DateUtils
import com.ledgerly.expense.domain.model.Expense
import com.ledgerly.expense.domain.repository.AuthRepository
import com.ledgerly.expense.domain.repository.ExpenseRepository
import com.ledgerly.expense.domain.repository.SyncRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Validates and persists an expense, then triggers a background sync. Keeping
 * this orchestration in a use case keeps ViewModels thin and testable.
 */
class SaveExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val authRepository: AuthRepository,
    private val syncScheduler: com.ledgerly.expense.data.sync.SyncScheduler,
) {
    suspend operator fun invoke(draft: ExpenseDraft): AppResult<String> {
        if (draft.amountCents <= 0) {
            return AppResult.Error(IllegalArgumentException("Amount must be greater than zero"))
        }
        if (draft.merchant.isBlank()) {
            return AppResult.Error(IllegalArgumentException("Merchant is required"))
        }
        val userId = authRepository.requireUserId()
        val now = DateUtils.nowMillis()
        val existing = draft.id?.let { expenseRepository.getById(it) }
        val expense = Expense(
            id = draft.id ?: UUID.randomUUID().toString(),
            date = draft.date,
            merchant = draft.merchant.trim(),
            amountCents = draft.amountCents,
            currency = draft.currency,
            expenseCategoryId = draft.categoryId,
            scheduleCLine = draft.scheduleCLine,
            paymentMethod = draft.paymentMethod,
            businessPurpose = draft.businessPurpose?.trim(),
            notes = draft.notes?.trim(),
            receiptLocalPath = draft.receiptLocalPath,
            receiptRemoteUrl = existing?.receiptRemoteUrl,
            mileage = draft.mileage,
            latitude = draft.latitude,
            longitude = draft.longitude,
            tags = draft.tags,
            clientId = draft.clientId,
            businessId = draft.businessId,
            recurringId = existing?.recurringId,
            remoteRowId = existing?.remoteRowId,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            ownerUserId = userId,
        )
        return when (val result = expenseRepository.upsert(expense)) {
            is AppResult.Success -> {
                syncScheduler.requestSync()
                AppResult.Success(expense.id)
            }
            is AppResult.Error -> result
        }
    }
}

/** Mutable form state captured by the expense entry screen. */
data class ExpenseDraft(
    val id: String? = null,
    val date: java.time.LocalDate,
    val merchant: String,
    val amountCents: Long,
    val currency: String = "USD",
    val categoryId: String,
    val scheduleCLine: String,
    val paymentMethod: String? = null,
    val businessPurpose: String? = null,
    val notes: String? = null,
    val receiptLocalPath: String? = null,
    val mileage: Double? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val tags: List<String> = emptyList(),
    val clientId: String? = null,
    val businessId: String? = null,
)

class DeleteExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val syncScheduler: com.ledgerly.expense.data.sync.SyncScheduler,
) {
    suspend operator fun invoke(id: String): AppResult<Unit> =
        expenseRepository.delete(id).also {
            if (it is AppResult.Success) syncScheduler.requestSync()
        }
}

/** Fire-and-forget convenience for triggering a sync from a coroutine scope. */
fun SyncRepository.syncInBackground(scope: CoroutineScope, userId: String) {
    scope.launch { syncNow(userId) }
}
