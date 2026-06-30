package com.ledgerly.expense.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ledgerly.expense.data.local.dao.BusinessDao
import com.ledgerly.expense.data.local.dao.CategoryDao
import com.ledgerly.expense.data.local.dao.ClientDao
import com.ledgerly.expense.data.local.dao.ExpenseDao
import com.ledgerly.expense.data.local.dao.MileageDao
import com.ledgerly.expense.data.local.dao.PaymentMethodDao
import com.ledgerly.expense.data.local.dao.RecurringExpenseDao
import com.ledgerly.expense.data.local.dao.SyncLogDao
import com.ledgerly.expense.data.local.entity.BusinessEntity
import com.ledgerly.expense.data.local.entity.CategoryEntity
import com.ledgerly.expense.data.local.entity.ClientEntity
import com.ledgerly.expense.data.local.entity.ExpenseEntity
import com.ledgerly.expense.data.local.entity.MileageTripEntity
import com.ledgerly.expense.data.local.entity.PaymentMethodEntity
import com.ledgerly.expense.data.local.entity.RecurringExpenseEntity
import com.ledgerly.expense.data.local.entity.SyncLogEntity

/**
 * The encrypted Room database. Opened with a SQLCipher [androidx.sqlite.db.SupportSQLiteOpenHelper.Factory]
 * keyed from the Android Keystore (see DatabaseModule). `exportSchema = true` keeps
 * versioned schema JSON under app/schemas for migration testing.
 */
@Database(
    entities = [
        ExpenseEntity::class,
        CategoryEntity::class,
        ClientEntity::class,
        BusinessEntity::class,
        PaymentMethodEntity::class,
        MileageTripEntity::class,
        RecurringExpenseEntity::class,
        SyncLogEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class LedgerlyDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun clientDao(): ClientDao
    abstract fun businessDao(): BusinessDao
    abstract fun paymentMethodDao(): PaymentMethodDao
    abstract fun mileageDao(): MileageDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao
    abstract fun syncLogDao(): SyncLogDao

    companion object {
        const val NAME = "ledgerly.db"
    }
}
