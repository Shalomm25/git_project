package com.ledgerly.expense.data.repository

import com.ledgerly.expense.core.AppResult
import com.ledgerly.expense.core.runCatchingResult
import com.ledgerly.expense.core.util.DateUtils
import com.ledgerly.expense.data.local.dao.ExpenseDao
import com.ledgerly.expense.data.local.dao.RecurringExpenseDao
import com.ledgerly.expense.data.local.toDomain
import com.ledgerly.expense.data.local.toEntity
import com.ledgerly.expense.di.IoDispatcher
import com.ledgerly.expense.domain.model.RecurrenceFrequency
import com.ledgerly.expense.domain.model.RecurringExpense
import com.ledgerly.expense.domain.model.SyncStatus
import com.ledgerly.expense.domain.repository.RecurringExpenseRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class RecurringExpenseRepositoryImpl @Inject constructor(
    private val recurringDao: RecurringExpenseDao,
    private val expenseDao: ExpenseDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) : RecurringExpenseRepository {

    override fun observeRecurring(userId: String): Flow<List<RecurringExpense>> =
        recurringDao.observeAll(userId).map { list -> list.map { it.toDomain() } }

    override suspend fun upsert(recurring: RecurringExpense): AppResult<Unit> =
        withContext(io) { runCatchingResult { recurringDao.upsert(recurring.toEntity()) } }

    override suspend fun delete(id: String): AppResult<Unit> =
        withContext(io) { runCatchingResult { recurringDao.delete(id) } }

    /**
     * Materializes a real expense for every recurring template whose next due
     * date has passed, then advances the schedule. Idempotent per due-date so
     * running it twice in a day never double-charges.
     */
    override suspend fun generateDue(userId: String): Int = withContext(io) {
        val today = DateUtils.today()
        val due = recurringDao.getDue(userId, today.toEpochDay())
        var created = 0
        for (entity in due) {
            val template = expenseDao.getById(entity.templateExpenseId) ?: continue
            var next = LocalDate.ofEpochDay(entity.nextDueDate)
            while (!next.isAfter(today)) {
                val now = DateUtils.nowMillis()
                val generated = template.copy(
                    id = UUID.randomUUID().toString(),
                    date = next.toEpochDay(),
                    recurringId = entity.id,
                    syncStatus = SyncStatus.PENDING,
                    remoteRowId = null,
                    createdAt = now,
                    updatedAt = now,
                    isDeleted = false,
                )
                expenseDao.upsert(generated)
                created++
                next = entity.frequency.advance(next)
            }
            recurringDao.upsert(
                entity.copy(nextDueDate = next.toEpochDay(), lastGeneratedAt = today.toEpochDay()),
            )
        }
        created
    }
}

/** Advance a date by one period of the given frequency. */
fun RecurrenceFrequency.advance(from: LocalDate): LocalDate = when (this) {
    RecurrenceFrequency.WEEKLY -> from.plusWeeks(1)
    RecurrenceFrequency.BIWEEKLY -> from.plusWeeks(2)
    RecurrenceFrequency.MONTHLY -> from.plusMonths(1)
    RecurrenceFrequency.QUARTERLY -> from.plusMonths(3)
    RecurrenceFrequency.YEARLY -> from.plusYears(1)
}
