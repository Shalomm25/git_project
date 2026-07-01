package com.ledgerly.expense.data.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.ledgerly.expense.data.local.dao.BusinessDao
import com.ledgerly.expense.data.local.dao.CategoryDao
import com.ledgerly.expense.data.local.dao.ClientDao
import com.ledgerly.expense.data.local.dao.ExpenseDao
import com.ledgerly.expense.data.local.toDomain
import com.ledgerly.expense.data.remote.ExpenseSheetSchema
import com.ledgerly.expense.di.IoDispatcher
import com.ledgerly.expense.domain.model.ExportFormat
import com.ledgerly.expense.domain.model.ScheduleCCategory
import com.ledgerly.expense.domain.repository.ReportRepository
import com.ledgerly.expense.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Produces a shareable report file for a tax year in the requested [ExportFormat]
 * and returns the [File]. CSV/Excel export the full transaction list; PDF renders
 * the Schedule C summary. Files land in the app's private `exports/` dir and are
 * shared via [FileProvider].
 */
@Singleton
class ReportExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
    private val clientDao: ClientDao,
    private val businessDao: BusinessDao,
    private val reportRepository: ReportRepository,
    private val settingsRepository: SettingsRepository,
    @IoDispatcher private val io: CoroutineDispatcher,
) {

    suspend fun export(userId: String, taxYear: Int, format: ExportFormat): File = withContext(io) {
        val dir = File(context.filesDir, "exports").apply { if (!exists()) mkdirs() }
        when (format) {
            ExportFormat.CSV -> File(dir, "ledgerly-$taxYear.csv").also {
                val (header, rows) = buildRows(userId, taxYear)
                CsvExporter.write(it, header, rows)
            }
            ExportFormat.EXCEL -> File(dir, "ledgerly-$taxYear.xlsx").also {
                val (header, rows) = buildRows(userId, taxYear)
                XlsxExporter.write(it, "Expenses $taxYear", header, rows)
            }
            ExportFormat.PDF -> File(dir, "ledgerly-schedule-c-$taxYear.pdf").also {
                val report = reportRepository.buildScheduleCReport(userId, taxYear)
                val currency = settingsRepository.settings.first().currencyCode
                PdfExporter.write(it, report, currency)
            }
            ExportFormat.GOOGLE_SHEETS -> error("Google Sheets export is handled by the sync engine")
        }
    }

    private suspend fun buildRows(userId: String, taxYear: Int): Pair<List<String>, List<List<Any?>>> {
        val start = LocalDate.of(taxYear, 1, 1).toEpochDay()
        val end = LocalDate.of(taxYear, 12, 31).toEpochDay()
        val expenses = expenseDao.getInRange(userId, start, end).map { it.toDomain() }
        val categories = categoryDao.observeAll(userId).first().associateBy { it.id }
        val clients = clientDao.observeAll(userId).first().associateBy { it.id }
        val businesses = businessDao.observeAll(userId).first().associateBy { it.id }

        val rows = expenses.map { expense ->
            val irs = ScheduleCCategory.fromLine(expense.scheduleCLine)
                ?.let { "Line ${it.line} — ${it.displayName}" } ?: expense.scheduleCLine
            ExpenseSheetSchema.toRow(
                expense = expense,
                categoryName = categories[expense.expenseCategoryId]?.name ?: "Uncategorized",
                irsCategoryName = irs,
                clientName = expense.clientId?.let { clients[it]?.name },
                businessName = expense.businessId?.let { businesses[it]?.name },
            )
        }
        return ExpenseSheetSchema.header to rows
    }

    /** Build a share [Intent] for an exported file. */
    fun shareIntent(file: File): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val mime = when (file.extension.lowercase()) {
            "pdf" -> "application/pdf"
            "csv" -> "text/csv"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            else -> "*/*"
        }
        return Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
