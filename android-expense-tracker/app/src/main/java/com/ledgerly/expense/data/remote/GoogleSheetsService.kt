package com.ledgerly.expense.data.remote

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.AddSheetRequest
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.ValueRange
import com.ledgerly.expense.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Thin coroutine wrapper around the blocking Google Sheets v4 client. Handles
 * spreadsheet creation, header seeding, and idempotent upserts keyed by the
 * Transaction ID in column A.
 */
class GoogleSheetsService @Inject constructor(
    private val factory: GoogleApiClientFactory,
    @IoDispatcher private val io: CoroutineDispatcher,
) {
    private fun client(): Sheets =
        factory.credentialOrNull()?.let(factory::sheets)
            ?: error("Google account not connected")

    /** Create the Ledgerly spreadsheet with a header row; returns its id. */
    suspend fun createSpreadsheet(title: String): String = withContext(io) {
        val sheets = client()
        val spreadsheet = Spreadsheet().apply {
            properties = SpreadsheetProperties().setTitle(title)
        }
        val created = sheets.spreadsheets().create(spreadsheet).execute()
        val id = created.spreadsheetId
        writeHeaderIfNeeded(id)
        id
    }

    suspend fun spreadsheetExists(spreadsheetId: String): Boolean = withContext(io) {
        runCatching { client().spreadsheets().get(spreadsheetId).execute() }.isSuccess
    }

    suspend fun spreadsheetUrl(spreadsheetId: String): String =
        "https://docs.google.com/spreadsheets/d/$spreadsheetId"

    private fun writeHeaderIfNeeded(spreadsheetId: String) {
        val sheets = client()
        val range = "${ExpenseSheetSchema.SHEET_TITLE}!A1"
        val existing = runCatching {
            sheets.spreadsheets().values()
                .get(spreadsheetId, "${ExpenseSheetSchema.SHEET_TITLE}!A1:A1").execute().getValues()
        }.getOrNull()
        if (existing.isNullOrEmpty()) {
            ensureSheetTab(spreadsheetId)
            sheets.spreadsheets().values()
                .update(spreadsheetId, range, ValueRange().setValues(listOf(ExpenseSheetSchema.header)))
                .setValueInputOption("RAW")
                .execute()
        }
    }

    private fun ensureSheetTab(spreadsheetId: String) {
        val sheets = client()
        val meta = sheets.spreadsheets().get(spreadsheetId).execute()
        val hasTab = meta.sheets.orEmpty().any {
            it.properties?.title == ExpenseSheetSchema.SHEET_TITLE
        }
        if (!hasTab) {
            val addSheet = Request().setAddSheet(
                AddSheetRequest().setProperties(
                    SheetProperties().setTitle(ExpenseSheetSchema.SHEET_TITLE),
                ),
            )
            sheets.spreadsheets().batchUpdate(
                spreadsheetId,
                BatchUpdateSpreadsheetRequest().setRequests(listOf(addSheet)),
            ).execute()
        }
    }

    /**
     * Upsert a single expense row. Returns the 1-based sheet row index used so
     * the caller can persist it as `remoteRowId` for fast future updates.
     */
    suspend fun upsertRow(
        spreadsheetId: String,
        transactionId: String,
        values: List<Any>,
        knownRowId: Long?,
    ): Long = withContext(io) {
        val sheets = client()
        writeHeaderIfNeeded(spreadsheetId)
        val rowId = knownRowId ?: findRowByTransactionId(spreadsheetId, transactionId)
        val tab = ExpenseSheetSchema.SHEET_TITLE
        if (rowId != null) {
            val range = "$tab!A$rowId"
            sheets.spreadsheets().values()
                .update(spreadsheetId, range, ValueRange().setValues(listOf(values)))
                .setValueInputOption("RAW")
                .execute()
            rowId
        } else {
            val append = sheets.spreadsheets().values()
                .append(spreadsheetId, "$tab!A:Q", ValueRange().setValues(listOf(values)))
                .setValueInputOption("RAW")
                .setInsertDataOption("INSERT_ROWS")
                .execute()
            // updatedRange looks like "Expenses!A42:Q42" — parse the row number.
            parseRowFromRange(append.updates?.updatedRange) ?: -1L
        }
    }

    private fun findRowByTransactionId(spreadsheetId: String, transactionId: String): Long? {
        val sheets = client()
        val column = runCatching {
            sheets.spreadsheets().values()
                .get(spreadsheetId, "${ExpenseSheetSchema.SHEET_TITLE}!A:A").execute().getValues()
        }.getOrNull() ?: return null
        column.forEachIndexed { index, row ->
            if (row.firstOrNull()?.toString() == transactionId) return (index + 1).toLong()
        }
        return null
    }

    private fun parseRowFromRange(range: String?): Long? =
        range?.substringAfterLast("!")?.let { Regex("\\d+").find(it)?.value?.toLongOrNull() }
}
