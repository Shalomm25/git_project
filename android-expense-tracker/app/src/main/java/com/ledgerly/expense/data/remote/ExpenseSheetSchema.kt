package com.ledgerly.expense.data.remote

import com.ledgerly.expense.core.util.DateUtils
import com.ledgerly.expense.core.util.Money
import com.ledgerly.expense.domain.model.Expense

/**
 * Defines the Google Sheets layout and the mapping between an [Expense] and a
 * spreadsheet row. Keeping it in one place guarantees the header and row order
 * stay in lockstep (see docs/GOOGLE_SHEETS_SETUP.md).
 */
object ExpenseSheetSchema {

    const val SHEET_TITLE = "Expenses"

    val header: List<String> = listOf(
        "Transaction ID", "Date", "Merchant", "Amount", "Expense Category", "IRS Category",
        "Payment Method", "Business Purpose", "Notes", "Receipt URL", "Mileage", "Client",
        "Business", "Tags", "Location", "Created Timestamp", "Updated Timestamp",
    )

    /** Convert an expense to a flat list of cell values matching [header]. */
    fun toRow(
        expense: Expense,
        categoryName: String,
        irsCategoryName: String,
        clientName: String?,
        businessName: String?,
    ): List<Any> = listOf(
        expense.id,
        DateUtils.formatIso(expense.date),
        expense.merchant,
        Money.centsToPlainString(expense.amountCents),
        categoryName,
        irsCategoryName,
        expense.paymentMethod.orEmpty(),
        expense.businessPurpose.orEmpty(),
        expense.notes.orEmpty(),
        expense.receiptRemoteUrl.orEmpty(),
        expense.mileage?.toString().orEmpty(),
        clientName.orEmpty(),
        businessName.orEmpty(),
        expense.tags.joinToString(", "),
        formatLocation(expense.latitude, expense.longitude),
        isoTimestamp(expense.createdAt),
        isoTimestamp(expense.updatedAt),
    )

    private fun formatLocation(lat: Double?, lng: Double?): String =
        if (lat != null && lng != null) "%.5f, %.5f".format(lat, lng) else ""

    private fun isoTimestamp(millis: Long): String =
        java.time.Instant.ofEpochMilli(millis).toString()
}
