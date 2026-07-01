package com.ledgerly.expense.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ledgerly.expense.domain.model.PaymentType
import com.ledgerly.expense.domain.model.RecurrenceFrequency
import com.ledgerly.expense.domain.model.SyncStatus

@Entity(tableName = "categories", indices = [Index(value = ["ownerUserId"])])
data class CategoryEntity(
    @PrimaryKey val id: String,
    val ownerUserId: String,
    val name: String,
    val scheduleCLine: String,
    val iconKey: String,
    val colorHex: String,
    val isDefault: Boolean,
    val sortOrder: Int,
)

@Entity(tableName = "clients", indices = [Index(value = ["ownerUserId"])])
data class ClientEntity(
    @PrimaryKey val id: String,
    val ownerUserId: String,
    val name: String,
    val contact: String?,
)

@Entity(tableName = "businesses", indices = [Index(value = ["ownerUserId"])])
data class BusinessEntity(
    @PrimaryKey val id: String,
    val ownerUserId: String,
    val name: String,
    val ein: String?,
)

@Entity(tableName = "payment_methods", indices = [Index(value = ["ownerUserId"])])
data class PaymentMethodEntity(
    @PrimaryKey val id: String,
    val ownerUserId: String,
    val name: String,
    val type: PaymentType,
)

@Entity(tableName = "mileage_trips", indices = [Index(value = ["ownerUserId", "date"])])
data class MileageTripEntity(
    @PrimaryKey val id: String,
    val ownerUserId: String,
    val date: Long,
    val startLocation: String,
    val endLocation: String,
    val totalMiles: Double,
    val businessPurpose: String?,
    val ratePerMileCents: Long,
    val expenseId: String?,
    @ColumnInfo(defaultValue = "PENDING") val syncStatus: SyncStatus,
)

@Entity(tableName = "recurring_expenses", indices = [Index(value = ["ownerUserId"])])
data class RecurringExpenseEntity(
    @PrimaryKey val id: String,
    val ownerUserId: String,
    val templateExpenseId: String,
    val frequency: RecurrenceFrequency,
    val nextDueDate: Long,
    val lastGeneratedAt: Long?,
    val isActive: Boolean,
)

/** Audit trail for sync attempts; powers retry/backoff and diagnostics. */
@Entity(tableName = "sync_log")
data class SyncLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,
    val entityId: String,
    val action: String,
    val status: String,
    val attempts: Int,
    val lastAttemptAt: Long,
    val errorMessage: String?,
)
