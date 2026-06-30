package com.ledgerly.expense.data.repository

import com.ledgerly.expense.core.util.DateUtils
import com.ledgerly.expense.data.local.dao.ExpenseDao
import com.ledgerly.expense.data.local.dao.MileageDao
import com.ledgerly.expense.data.local.toDomain
import com.ledgerly.expense.di.IoDispatcher
import com.ledgerly.expense.domain.model.ScheduleCCategory
import com.ledgerly.expense.domain.model.ScheduleCLineTotal
import com.ledgerly.expense.domain.model.ScheduleCReport
import com.ledgerly.expense.domain.repository.ReportRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

/**
 * Builds tax-ready reports by rolling up expenses into IRS Schedule C lines.
 * Mileage is converted to a deduction using the configured standard rate and
 * surfaced separately (it flows into Schedule C line 9 / Form 8829 workflows).
 */
class ReportRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val mileageDao: MileageDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) : ReportRepository {

    override suspend fun buildScheduleCReport(userId: String, taxYear: Int): ScheduleCReport =
        withContext(io) {
            val start = LocalDate.of(taxYear, 1, 1).toEpochDay()
            val end = LocalDate.of(taxYear, 12, 31).toEpochDay()

            val expenses = expenseDao.observeInRange(userId, start, end).first().map { it.toDomain() }
            val byLine = expenses
                .groupBy { it.scheduleCLine }
                .mapNotNull { (line, items) ->
                    val category = ScheduleCCategory.fromLine(line) ?: return@mapNotNull null
                    ScheduleCLineTotal(
                        category = category,
                        totalCents = items.sumOf { it.amountCents },
                        expenseCount = items.size,
                    )
                }
                .sortedBy { it.category.line }

            val trips = mileageDao.inRange(userId, start, end).map { it.toDomain() }
            val totalMiles = trips.sumOf { it.totalMiles }
            val mileageDeduction = trips.sumOf { it.deductionCents }

            ScheduleCReport(
                taxYear = taxYear,
                lines = byLine,
                mileageTotalMiles = totalMiles,
                mileageDeductionCents = mileageDeduction,
                generatedAt = DateUtils.nowMillis(),
            )
        }
}
