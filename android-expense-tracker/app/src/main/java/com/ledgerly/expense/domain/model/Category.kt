package com.ledgerly.expense.domain.model

/**
 * A user-facing expense category. Each maps to a [ScheduleCCategory] line so
 * categorizing an expense automatically classifies it for tax reporting.
 * Users can add/edit/delete categories; defaults are seeded on first run.
 */
data class Category(
    val id: String,
    val name: String,
    val scheduleCLine: String,
    val iconKey: String = "receipt",
    val colorHex: String = "#0E7C5A",
    val isDefault: Boolean = false,
    val sortOrder: Int = 0,
    val ownerUserId: String,
) {
    val scheduleC: ScheduleCCategory?
        get() = ScheduleCCategory.fromLine(scheduleCLine)
}
