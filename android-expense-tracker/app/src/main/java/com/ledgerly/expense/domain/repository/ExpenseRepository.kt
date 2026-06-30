package com.ledgerly.expense.domain.repository

import com.ledgerly.expense.core.AppResult
import com.ledgerly.expense.domain.model.DashboardSummary
import com.ledgerly.expense.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Abstraction over expense storage. The UI/use cases depend on this interface,
 * not on Room — keeping the domain layer free of Android/persistence concerns.
 */
interface ExpenseRepository {

    fun observeExpenses(userId: String): Flow<List<Expense>>

    fun observeExpense(id: String): Flow<Expense?>

    fun observeDashboard(userId: String, today: LocalDate): Flow<DashboardSummary>

    /** Full-text-ish search across merchant, notes, category, client, business. */
    fun search(userId: String, query: ExpenseQuery): Flow<List<Expense>>

    suspend fun getById(id: String): Expense?

    /** Insert or update; marks the record [com.ledgerly.expense.domain.model.SyncStatus.PENDING]. */
    suspend fun upsert(expense: Expense): AppResult<Unit>

    /** Soft-delete so the deletion can be propagated to the remote sheet. */
    suspend fun delete(id: String): AppResult<Unit>

    suspend fun getPendingSync(userId: String): List<Expense>

    suspend fun markSynced(id: String, remoteRowId: Long?)

    suspend fun markFailed(id: String)
}

/** Composable search/filter criteria for the search screen. */
data class ExpenseQuery(
    val text: String? = null,
    val categoryId: String? = null,
    val clientId: String? = null,
    val businessId: String? = null,
    val from: LocalDate? = null,
    val to: LocalDate? = null,
    val minCents: Long? = null,
    val maxCents: Long? = null,
)
