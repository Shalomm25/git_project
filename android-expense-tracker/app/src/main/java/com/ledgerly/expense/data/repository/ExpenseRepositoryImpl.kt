package com.ledgerly.expense.data.repository

import com.ledgerly.expense.core.AppResult
import com.ledgerly.expense.core.runCatchingResult
import com.ledgerly.expense.core.util.DateUtils
import com.ledgerly.expense.data.local.dao.CategoryDao
import com.ledgerly.expense.data.local.dao.ExpenseDao
import com.ledgerly.expense.data.local.toDomain
import com.ledgerly.expense.data.local.toEntity
import com.ledgerly.expense.di.IoDispatcher
import com.ledgerly.expense.domain.model.CategoryTotal
import com.ledgerly.expense.domain.model.DashboardSummary
import com.ledgerly.expense.domain.model.Expense
import com.ledgerly.expense.domain.model.MonthTotal
import com.ledgerly.expense.domain.model.SyncStatus
import com.ledgerly.expense.domain.model.VendorTotal
import com.ledgerly.expense.domain.repository.ExpenseQuery
import com.ledgerly.expense.domain.repository.ExpenseRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) : ExpenseRepository {

    override fun observeExpenses(userId: String): Flow<List<Expense>> =
        expenseDao.observeAll(userId).map { list -> list.map { it.toDomain() } }

    override fun observeExpense(id: String): Flow<Expense?> =
        expenseDao.observeById(id).map { it?.toDomain() }

    override fun search(userId: String, query: ExpenseQuery): Flow<List<Expense>> =
        expenseDao.search(
            userId = userId,
            text = query.text?.takeIf { it.isNotBlank() },
            categoryId = query.categoryId,
            clientId = query.clientId,
            businessId = query.businessId,
            fromDay = query.from?.toEpochDay(),
            toDay = query.to?.toEpochDay(),
            minCents = query.minCents,
            maxCents = query.maxCents,
        ).map { list -> list.map { it.toDomain() } }

    override fun observeDashboard(userId: String, today: LocalDate): Flow<DashboardSummary> {
        val startWeek = DateUtils.startOfWeek(today).toEpochDay()
        val startMonth = DateUtils.startOfMonth(today).toEpochDay()
        val startYear = DateUtils.startOfYear(today).toEpochDay()
        val todayDay = today.toEpochDay()

        // Combine the independent aggregate flows. Room emits on any underlying
        // change so the dashboard stays live without manual refresh.
        val totals = combine(
            expenseDao.observeTotalInRange(userId, todayDay, todayDay),
            expenseDao.observeTotalInRange(userId, startWeek, todayDay),
            expenseDao.observeTotalInRange(userId, startMonth, todayDay),
            expenseDao.observeTotalInRange(userId, startYear, todayDay),
        ) { day, week, month, year -> Totals(day, week, month, year) }

        val breakdowns = combine(
            expenseDao.observeCategoryTotals(userId, startYear, todayDay),
            categoryDao.observeAll(userId),
            expenseDao.observeTopVendors(userId, startYear, todayDay, VENDOR_LIMIT),
            expenseDao.observeDailyTotals(userId, startYear, todayDay),
        ) { catAgg, categories, vendorAgg, daily ->
            val nameById = categories.associate { it.id to (it.name to it.colorHex) }
            val byCategory = catAgg.map { agg ->
                val (name, color) = nameById[agg.expenseCategoryId] ?: ("Uncategorized" to "#868E96")
                CategoryTotal(agg.expenseCategoryId, name, color, agg.total, agg.cnt)
            }
            val byMonth = daily
                .groupBy { YearMonth.from(LocalDate.ofEpochDay(it.epochDay)) }
                .map { (ym, rows) -> MonthTotal(ym, rows.sumOf { it.amountCents }) }
                .sortedBy { it.month }
            val topVendors = vendorAgg.map { VendorTotal(it.merchant, it.total, it.cnt) }
            Breakdowns(byCategory, byMonth, topVendors)
        }

        val lists = combine(
            expenseDao.observeLargest(userId, startYear, todayDay, LIST_LIMIT),
            expenseDao.observeRecent(userId, LIST_LIMIT),
            expenseDao.observeMissingReceiptCount(userId),
            expenseDao.observeCount(userId),
        ) { largest, recent, missing, count ->
            Lists(largest.map { it.toDomain() }, recent.map { it.toDomain() }, missing, count)
        }

        return combine(totals, breakdowns, lists) { t, b, l ->
            DashboardSummary(
                todayCents = t.day,
                weekCents = t.week,
                monthCents = t.month,
                yearCents = t.year,
                expenseCount = l.count,
                missingReceiptCount = l.missing,
                byCategory = b.byCategory,
                byMonth = b.byMonth,
                largestExpenses = l.largest,
                recentExpenses = l.recent,
                topVendors = b.topVendors,
            )
        }
    }

    override suspend fun getById(id: String): Expense? =
        withContext(io) { expenseDao.getById(id)?.toDomain() }

    override suspend fun upsert(expense: Expense): AppResult<Unit> = withContext(io) {
        runCatchingResult {
            expenseDao.upsert(expense.copy(syncStatus = SyncStatus.PENDING).toEntity())
        }
    }

    override suspend fun delete(id: String): AppResult<Unit> = withContext(io) {
        runCatchingResult { expenseDao.softDelete(id, DateUtils.nowMillis()) }
    }

    override suspend fun getPendingSync(userId: String): List<Expense> =
        withContext(io) { expenseDao.getPendingSync(userId).map { it.toDomain() } }

    override suspend fun markSynced(id: String, remoteRowId: Long?) =
        withContext(io) { expenseDao.updateSyncStatus(id, SyncStatus.SYNCED, remoteRowId) }

    override suspend fun markFailed(id: String) =
        withContext(io) { expenseDao.updateSyncStatus(id, SyncStatus.FAILED, null) }

    private data class Totals(val day: Long, val week: Long, val month: Long, val year: Long)
    private data class Breakdowns(
        val byCategory: List<CategoryTotal>,
        val byMonth: List<MonthTotal>,
        val topVendors: List<VendorTotal>,
    )
    private data class Lists(
        val largest: List<Expense>,
        val recent: List<Expense>,
        val missing: Int,
        val count: Int,
    )

    private companion object {
        const val VENDOR_LIMIT = 5
        const val LIST_LIMIT = 5
    }
}
