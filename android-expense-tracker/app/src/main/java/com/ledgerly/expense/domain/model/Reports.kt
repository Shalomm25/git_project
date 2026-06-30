package com.ledgerly.expense.domain.model

import java.time.LocalDate
import java.time.YearMonth

/** Aggregated totals shown on the dashboard. */
data class DashboardSummary(
    val todayCents: Long = 0,
    val weekCents: Long = 0,
    val monthCents: Long = 0,
    val yearCents: Long = 0,
    val expenseCount: Int = 0,
    val missingReceiptCount: Int = 0,
    val byCategory: List<CategoryTotal> = emptyList(),
    val byMonth: List<MonthTotal> = emptyList(),
    val largestExpenses: List<Expense> = emptyList(),
    val recentExpenses: List<Expense> = emptyList(),
    val topVendors: List<VendorTotal> = emptyList(),
)

data class CategoryTotal(
    val categoryId: String,
    val categoryName: String,
    val colorHex: String,
    val totalCents: Long,
    val count: Int,
) {
    fun share(of: Long): Float = if (of <= 0) 0f else totalCents.toFloat() / of
}

data class MonthTotal(val month: YearMonth, val totalCents: Long)

data class VendorTotal(val merchant: String, val totalCents: Long, val count: Int)

/** A single Schedule C line rollup for the tax summary report. */
data class ScheduleCLineTotal(
    val category: ScheduleCCategory,
    val totalCents: Long,
    val expenseCount: Int,
)

/** The complete tax-ready Schedule C summary for a tax year. */
data class ScheduleCReport(
    val taxYear: Int,
    val lines: List<ScheduleCLineTotal>,
    val mileageTotalMiles: Double,
    val mileageDeductionCents: Long,
    val generatedAt: Long,
) {
    val totalDeductibleCents: Long
        get() = lines.sumOf { it.totalCents } + mileageDeductionCents
}

enum class ReportPeriod { MONTHLY, QUARTERLY, ANNUAL }

enum class ExportFormat { PDF, CSV, EXCEL, GOOGLE_SHEETS }

data class DateRange(val start: LocalDate, val end: LocalDate) {
    operator fun contains(date: LocalDate): Boolean = date in start..end
}
