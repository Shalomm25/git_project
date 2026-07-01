package com.ledgerly.expense.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAdjusters

/**
 * Date helpers. Dates are stored as epoch days (LocalDate) and timestamps as
 * epoch millis (UTC) so they sort and sync deterministically.
 */
object DateUtils {

    private val displayFormatter: DateTimeFormatter =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val isoDate: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun today(): LocalDate = LocalDate.now()

    fun nowMillis(): Long = Instant.now().toEpochMilli()

    fun formatDisplay(date: LocalDate): String = date.format(displayFormatter)

    fun formatIso(date: LocalDate): String = date.format(isoDate)

    fun millisToLocalDate(millis: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    fun startOfWeek(date: LocalDate = today()): LocalDate =
        date.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))

    fun startOfMonth(date: LocalDate = today()): LocalDate = date.withDayOfMonth(1)

    fun startOfYear(date: LocalDate = today()): LocalDate = date.withDayOfYear(1)

    fun quarterOf(date: LocalDate): Int = date.get(IsoFields.QUARTER_OF_YEAR)

    fun quarterRange(year: Int, quarter: Int): ClosedRange<LocalDate> {
        val startMonth = (quarter - 1) * 3 + 1
        val start = LocalDate.of(year, startMonth, 1)
        val end = start.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth())
        return start..end
    }

    fun monthLabel(yearMonth: YearMonth): String =
        yearMonth.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault())
}
