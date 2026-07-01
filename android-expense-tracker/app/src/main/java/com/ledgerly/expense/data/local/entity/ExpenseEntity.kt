package com.ledgerly.expense.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ledgerly.expense.domain.model.SyncStatus

/**
 * Room entity for an expense. Mirrors [com.ledgerly.expense.domain.model.Expense].
 * Indexed by owner+date for fast dashboard/range queries and by syncStatus for
 * the sync engine. Soft-deletes via [isDeleted] so removals propagate to Sheets.
 */
@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["ownerUserId", "date"]),
        Index(value = ["ownerUserId", "syncStatus"]),
        Index(value = ["expenseCategoryId"]),
        Index(value = ["merchant"]),
    ],
)
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val ownerUserId: String,
    /** Epoch day (LocalDate.toEpochDay) for deterministic range queries. */
    val date: Long,
    val merchant: String,
    val amountCents: Long,
    val currency: String,
    val expenseCategoryId: String,
    val scheduleCLine: String,
    val paymentMethod: String?,
    val businessPurpose: String?,
    val notes: String?,
    val receiptLocalPath: String?,
    val receiptRemoteUrl: String?,
    val mileage: Double?,
    val latitude: Double?,
    val longitude: Double?,
    /** Comma-separated tags; small and queryable with LIKE. */
    val tags: String?,
    val clientId: String?,
    val businessId: String?,
    val recurringId: String?,
    @ColumnInfo(defaultValue = "PENDING") val syncStatus: SyncStatus,
    val remoteRowId: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    @ColumnInfo(defaultValue = "0") val isDeleted: Boolean,
)
