package com.ledgerly.expense.di

import android.content.Context
import androidx.room.Room
import com.ledgerly.expense.data.local.LedgerlyDatabase
import com.ledgerly.expense.data.local.dao.BusinessDao
import com.ledgerly.expense.data.local.dao.CategoryDao
import com.ledgerly.expense.data.local.dao.ClientDao
import com.ledgerly.expense.data.local.dao.ExpenseDao
import com.ledgerly.expense.data.local.dao.MileageDao
import com.ledgerly.expense.data.local.dao.PaymentMethodDao
import com.ledgerly.expense.data.local.dao.RecurringExpenseDao
import com.ledgerly.expense.data.local.dao.SyncLogDao
import com.ledgerly.expense.data.security.SecurePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

/**
 * Provides the SQLCipher-encrypted Room database. The passphrase is generated
 * once and stored in the Keystore-backed [SecurePreferences], so the database
 * file on disk is unreadable without the device's hardware-backed key.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        securePreferences: SecurePreferences,
    ): LedgerlyDatabase {
        // Load the native SQLCipher library before opening the database.
        System.loadLibrary("sqlcipher")
        val passphrase = securePreferences.getOrCreateDatabasePassphrase()
        val factory = SupportOpenHelperFactory(passphrase)
        return Room.databaseBuilder(context, LedgerlyDatabase::class.java, LedgerlyDatabase.NAME)
            .openHelperFactory(factory)
            .fallbackToDestructiveMigrationOnDowngrade()
            // Real migrations are added here as the schema evolves (see DATABASE_SCHEMA.md).
            .build()
    }

    @Provides fun provideExpenseDao(db: LedgerlyDatabase): ExpenseDao = db.expenseDao()
    @Provides fun provideCategoryDao(db: LedgerlyDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideClientDao(db: LedgerlyDatabase): ClientDao = db.clientDao()
    @Provides fun provideBusinessDao(db: LedgerlyDatabase): BusinessDao = db.businessDao()
    @Provides fun providePaymentMethodDao(db: LedgerlyDatabase): PaymentMethodDao = db.paymentMethodDao()
    @Provides fun provideMileageDao(db: LedgerlyDatabase): MileageDao = db.mileageDao()
    @Provides fun provideRecurringDao(db: LedgerlyDatabase): RecurringExpenseDao = db.recurringExpenseDao()
    @Provides fun provideSyncLogDao(db: LedgerlyDatabase): SyncLogDao = db.syncLogDao()
}
