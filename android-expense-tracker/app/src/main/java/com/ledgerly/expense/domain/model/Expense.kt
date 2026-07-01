package com.ledgerly.expense.domain.model

import java.time.LocalDate

/**
 * Core domain model for a business expense. Amounts are in integer cents.
 * This is the layer-independent representation used by use cases and the UI;
 * the Room entity in the data layer mirrors it.
 */
data class Expense(
    val id: String,
    val date: LocalDate,
    val merchant: String,
    val amountCents: Long,
    val currency: String = "USD",
    val expenseCategoryId: String,
    val scheduleCLine: String,
    val paymentMethod: String? = null,
    val businessPurpose: String? = null,
    val notes: String? = null,
    val receiptLocalPath: String? = null,
    val receiptRemoteUrl: String? = null,
    val mileage: Double? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val tags: List<String> = emptyList(),
    val clientId: String? = null,
    val businessId: String? = null,
    val recurringId: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val remoteRowId: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val ownerUserId: String,
) {
    val hasReceipt: Boolean get() = receiptLocalPath != null || receiptRemoteUrl != null
}
