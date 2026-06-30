package com.ledgerly.expense.di

import com.ledgerly.expense.data.repository.BusinessRepositoryImpl
import com.ledgerly.expense.data.repository.CategoryRepositoryImpl
import com.ledgerly.expense.data.repository.ClientRepositoryImpl
import com.ledgerly.expense.data.repository.ExpenseRepositoryImpl
import com.ledgerly.expense.data.repository.FirebaseAuthRepository
import com.ledgerly.expense.data.repository.MileageRepositoryImpl
import com.ledgerly.expense.data.repository.PaymentMethodRepositoryImpl
import com.ledgerly.expense.data.repository.RecurringExpenseRepositoryImpl
import com.ledgerly.expense.data.repository.ReportRepositoryImpl
import com.ledgerly.expense.data.repository.SettingsRepositoryImpl
import com.ledgerly.expense.data.sync.SyncRepositoryImpl
import com.ledgerly.expense.domain.repository.AuthRepository
import com.ledgerly.expense.domain.repository.BusinessRepository
import com.ledgerly.expense.domain.repository.CategoryRepository
import com.ledgerly.expense.domain.repository.ClientRepository
import com.ledgerly.expense.domain.repository.ExpenseRepository
import com.ledgerly.expense.domain.repository.MileageRepository
import com.ledgerly.expense.domain.repository.PaymentMethodRepository
import com.ledgerly.expense.domain.repository.RecurringExpenseRepository
import com.ledgerly.expense.domain.repository.ReportRepository
import com.ledgerly.expense.domain.repository.SettingsRepository
import com.ledgerly.expense.domain.repository.SyncRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Binds repository interfaces to their concrete implementations. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository

    @Binds @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds @Singleton
    abstract fun bindClientRepository(impl: ClientRepositoryImpl): ClientRepository

    @Binds @Singleton
    abstract fun bindBusinessRepository(impl: BusinessRepositoryImpl): BusinessRepository

    @Binds @Singleton
    abstract fun bindPaymentMethodRepository(impl: PaymentMethodRepositoryImpl): PaymentMethodRepository

    @Binds @Singleton
    abstract fun bindMileageRepository(impl: MileageRepositoryImpl): MileageRepository

    @Binds @Singleton
    abstract fun bindRecurringRepository(impl: RecurringExpenseRepositoryImpl): RecurringExpenseRepository

    @Binds @Singleton
    abstract fun bindReportRepository(impl: ReportRepositoryImpl): ReportRepository

    @Binds @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    @Binds @Singleton
    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository
}
