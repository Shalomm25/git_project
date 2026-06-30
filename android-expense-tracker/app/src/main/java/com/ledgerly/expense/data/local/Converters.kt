package com.ledgerly.expense.data.local

import androidx.room.TypeConverter
import com.ledgerly.expense.domain.model.PaymentType
import com.ledgerly.expense.domain.model.RecurrenceFrequency
import com.ledgerly.expense.domain.model.SyncStatus

/** Room type converters for enums stored as their stable names. */
class Converters {
    @TypeConverter fun syncStatusToString(value: SyncStatus): String = value.name
    @TypeConverter fun stringToSyncStatus(value: String): SyncStatus =
        runCatching { SyncStatus.valueOf(value) }.getOrDefault(SyncStatus.PENDING)

    @TypeConverter fun paymentTypeToString(value: PaymentType): String = value.name
    @TypeConverter fun stringToPaymentType(value: String): PaymentType =
        runCatching { PaymentType.valueOf(value) }.getOrDefault(PaymentType.OTHER)

    @TypeConverter fun frequencyToString(value: RecurrenceFrequency): String = value.name
    @TypeConverter fun stringToFrequency(value: String): RecurrenceFrequency =
        runCatching { RecurrenceFrequency.valueOf(value) }.getOrDefault(RecurrenceFrequency.MONTHLY)
}
