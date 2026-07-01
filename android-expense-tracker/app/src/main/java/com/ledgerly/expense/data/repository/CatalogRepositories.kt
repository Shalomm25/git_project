package com.ledgerly.expense.data.repository

import com.ledgerly.expense.core.AppResult
import com.ledgerly.expense.core.runCatchingResult
import com.ledgerly.expense.data.local.DefaultData
import com.ledgerly.expense.data.local.dao.BusinessDao
import com.ledgerly.expense.data.local.dao.CategoryDao
import com.ledgerly.expense.data.local.dao.ClientDao
import com.ledgerly.expense.data.local.dao.MileageDao
import com.ledgerly.expense.data.local.dao.PaymentMethodDao
import com.ledgerly.expense.data.local.toDomain
import com.ledgerly.expense.data.local.toEntity
import com.ledgerly.expense.di.IoDispatcher
import com.ledgerly.expense.domain.model.Business
import com.ledgerly.expense.domain.model.Category
import com.ledgerly.expense.domain.model.Client
import com.ledgerly.expense.domain.model.MileageTrip
import com.ledgerly.expense.domain.model.PaymentMethod
import com.ledgerly.expense.domain.repository.BusinessRepository
import com.ledgerly.expense.domain.repository.CategoryRepository
import com.ledgerly.expense.domain.repository.ClientRepository
import com.ledgerly.expense.domain.repository.MileageRepository
import com.ledgerly.expense.domain.repository.PaymentMethodRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val dao: CategoryDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) : CategoryRepository {
    override fun observeCategories(userId: String): Flow<List<Category>> =
        dao.observeAll(userId).map { list -> list.map { it.toDomain() } }

    override suspend fun upsert(category: Category): AppResult<Unit> =
        withContext(io) { runCatchingResult { dao.upsert(category.toEntity()) } }

    override suspend fun delete(id: String): AppResult<Unit> =
        withContext(io) { runCatchingResult { dao.delete(id) } }

    override suspend fun seedDefaultsIfEmpty(userId: String) = withContext(io) {
        if (dao.count(userId) == 0) dao.upsertAll(DefaultData.defaultCategories(userId))
    }
}

class ClientRepositoryImpl @Inject constructor(
    private val dao: ClientDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) : ClientRepository {
    override fun observeClients(userId: String): Flow<List<Client>> =
        dao.observeAll(userId).map { list -> list.map { it.toDomain() } }
    override suspend fun upsert(client: Client): AppResult<Unit> =
        withContext(io) { runCatchingResult { dao.upsert(client.toEntity()) } }
    override suspend fun delete(id: String): AppResult<Unit> =
        withContext(io) { runCatchingResult { dao.delete(id) } }
}

class BusinessRepositoryImpl @Inject constructor(
    private val dao: BusinessDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) : BusinessRepository {
    override fun observeBusinesses(userId: String): Flow<List<Business>> =
        dao.observeAll(userId).map { list -> list.map { it.toDomain() } }
    override suspend fun upsert(business: Business): AppResult<Unit> =
        withContext(io) { runCatchingResult { dao.upsert(business.toEntity()) } }
    override suspend fun delete(id: String): AppResult<Unit> =
        withContext(io) { runCatchingResult { dao.delete(id) } }
}

class PaymentMethodRepositoryImpl @Inject constructor(
    private val dao: PaymentMethodDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) : PaymentMethodRepository {
    override fun observePaymentMethods(userId: String): Flow<List<PaymentMethod>> =
        dao.observeAll(userId).map { list -> list.map { it.toDomain() } }
    override suspend fun upsert(method: PaymentMethod): AppResult<Unit> =
        withContext(io) { runCatchingResult { dao.upsert(method.toEntity()) } }
    override suspend fun delete(id: String): AppResult<Unit> =
        withContext(io) { runCatchingResult { dao.delete(id) } }
    override suspend fun seedDefaultsIfEmpty(userId: String) = withContext(io) {
        if (dao.count(userId) == 0) dao.upsertAll(DefaultData.defaultPaymentMethods(userId))
    }
}

class MileageRepositoryImpl @Inject constructor(
    private val dao: MileageDao,
    @IoDispatcher private val io: CoroutineDispatcher,
) : MileageRepository {
    override fun observeTrips(userId: String): Flow<List<MileageTrip>> =
        dao.observeAll(userId).map { list -> list.map { it.toDomain() } }
    override suspend fun upsert(trip: MileageTrip): AppResult<Unit> =
        withContext(io) { runCatchingResult { dao.upsert(trip.toEntity()) } }
    override suspend fun delete(id: String): AppResult<Unit> =
        withContext(io) { runCatchingResult { dao.delete(id) } }
}
