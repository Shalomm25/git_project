package com.ledgerly.expense.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ledgerly.expense.data.local.entity.BusinessEntity
import com.ledgerly.expense.data.local.entity.CategoryEntity
import com.ledgerly.expense.data.local.entity.ClientEntity
import com.ledgerly.expense.data.local.entity.MileageTripEntity
import com.ledgerly.expense.data.local.entity.PaymentMethodEntity
import com.ledgerly.expense.data.local.entity.RecurringExpenseEntity
import com.ledgerly.expense.data.local.entity.SyncLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Upsert suspend fun upsert(category: CategoryEntity)
    @Upsert suspend fun upsertAll(categories: List<CategoryEntity>)
    @Query("SELECT * FROM categories WHERE ownerUserId = :userId ORDER BY sortOrder, name")
    fun observeAll(userId: String): Flow<List<CategoryEntity>>
    @Query("SELECT COUNT(*) FROM categories WHERE ownerUserId = :userId")
    suspend fun count(userId: String): Int
    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface ClientDao {
    @Upsert suspend fun upsert(client: ClientEntity)
    @Query("SELECT * FROM clients WHERE ownerUserId = :userId ORDER BY name")
    fun observeAll(userId: String): Flow<List<ClientEntity>>
    @Query("DELETE FROM clients WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface BusinessDao {
    @Upsert suspend fun upsert(business: BusinessEntity)
    @Query("SELECT * FROM businesses WHERE ownerUserId = :userId ORDER BY name")
    fun observeAll(userId: String): Flow<List<BusinessEntity>>
    @Query("DELETE FROM businesses WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface PaymentMethodDao {
    @Upsert suspend fun upsert(method: PaymentMethodEntity)
    @Upsert suspend fun upsertAll(methods: List<PaymentMethodEntity>)
    @Query("SELECT * FROM payment_methods WHERE ownerUserId = :userId ORDER BY name")
    fun observeAll(userId: String): Flow<List<PaymentMethodEntity>>
    @Query("SELECT COUNT(*) FROM payment_methods WHERE ownerUserId = :userId")
    suspend fun count(userId: String): Int
    @Query("DELETE FROM payment_methods WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface MileageDao {
    @Upsert suspend fun upsert(trip: MileageTripEntity)
    @Query("SELECT * FROM mileage_trips WHERE ownerUserId = :userId ORDER BY date DESC")
    fun observeAll(userId: String): Flow<List<MileageTripEntity>>
    @Query(
        "SELECT * FROM mileage_trips WHERE ownerUserId = :userId AND date BETWEEN :startDay AND :endDay"
    )
    suspend fun inRange(userId: String, startDay: Long, endDay: Long): List<MileageTripEntity>
    @Query("DELETE FROM mileage_trips WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface RecurringExpenseDao {
    @Upsert suspend fun upsert(recurring: RecurringExpenseEntity)
    @Query("SELECT * FROM recurring_expenses WHERE ownerUserId = :userId ORDER BY nextDueDate")
    fun observeAll(userId: String): Flow<List<RecurringExpenseEntity>>
    @Query(
        "SELECT * FROM recurring_expenses WHERE ownerUserId = :userId AND isActive = 1 " +
            "AND nextDueDate <= :throughDay"
    )
    suspend fun getDue(userId: String, throughDay: Long): List<RecurringExpenseEntity>
    @Query("DELETE FROM recurring_expenses WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface SyncLogDao {
    @Upsert suspend fun upsert(log: SyncLogEntity)
    @Query("SELECT * FROM sync_log ORDER BY lastAttemptAt DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<SyncLogEntity>
    @Query("DELETE FROM sync_log WHERE lastAttemptAt < :before")
    suspend fun prune(before: Long)
}
