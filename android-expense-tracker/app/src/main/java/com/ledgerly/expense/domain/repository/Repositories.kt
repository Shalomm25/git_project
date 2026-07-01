package com.ledgerly.expense.domain.repository

import com.ledgerly.expense.core.AppResult
import com.ledgerly.expense.domain.model.Business
import com.ledgerly.expense.domain.model.Category
import com.ledgerly.expense.domain.model.Client
import com.ledgerly.expense.domain.model.MileageTrip
import com.ledgerly.expense.domain.model.PaymentMethod
import com.ledgerly.expense.domain.model.RecurringExpense
import com.ledgerly.expense.domain.model.ScheduleCReport
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(userId: String): Flow<List<Category>>
    suspend fun upsert(category: Category): AppResult<Unit>
    suspend fun delete(id: String): AppResult<Unit>
    /** Seeds the default IRS-mapped categories for a new user. */
    suspend fun seedDefaultsIfEmpty(userId: String)
}

interface ClientRepository {
    fun observeClients(userId: String): Flow<List<Client>>
    suspend fun upsert(client: Client): AppResult<Unit>
    suspend fun delete(id: String): AppResult<Unit>
}

interface BusinessRepository {
    fun observeBusinesses(userId: String): Flow<List<Business>>
    suspend fun upsert(business: Business): AppResult<Unit>
    suspend fun delete(id: String): AppResult<Unit>
}

interface PaymentMethodRepository {
    fun observePaymentMethods(userId: String): Flow<List<PaymentMethod>>
    suspend fun upsert(method: PaymentMethod): AppResult<Unit>
    suspend fun delete(id: String): AppResult<Unit>
    suspend fun seedDefaultsIfEmpty(userId: String)
}

interface MileageRepository {
    fun observeTrips(userId: String): Flow<List<MileageTrip>>
    suspend fun upsert(trip: MileageTrip): AppResult<Unit>
    suspend fun delete(id: String): AppResult<Unit>
}

interface RecurringExpenseRepository {
    fun observeRecurring(userId: String): Flow<List<RecurringExpense>>
    suspend fun upsert(recurring: RecurringExpense): AppResult<Unit>
    suspend fun delete(id: String): AppResult<Unit>
    /** Generate any due expenses from active templates; returns count created. */
    suspend fun generateDue(userId: String): Int
}

interface ReportRepository {
    suspend fun buildScheduleCReport(userId: String, taxYear: Int): ScheduleCReport
}
