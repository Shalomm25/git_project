package com.ledgerly.expense.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.ledgerly.expense.data.local.entity.ExpenseEntity
import com.ledgerly.expense.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

/** Aggregate row used by dashboard category/vendor rollups. */
data class CategoryAggregate(val expenseCategoryId: String, val total: Long, val cnt: Int)
data class VendorAggregate(val merchant: String, val total: Long, val cnt: Int)
data class MonthAggregate(val epochDay: Long, val amountCents: Long)

@Dao
interface ExpenseDao {

    @Upsert
    suspend fun upsert(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<ExpenseEntity>)

    @Query("SELECT * FROM expenses WHERE id = :id AND isDeleted = 0")
    suspend fun getById(id: String): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE id = :id")
    fun observeById(id: String): Flow<ExpenseEntity?>

    @Query(
        "SELECT * FROM expenses WHERE ownerUserId = :userId AND isDeleted = 0 " +
            "ORDER BY date DESC, createdAt DESC"
    )
    fun observeAll(userId: String): Flow<List<ExpenseEntity>>

    @Query(
        "SELECT * FROM expenses WHERE ownerUserId = :userId AND isDeleted = 0 " +
            "AND date BETWEEN :startDay AND :endDay ORDER BY date DESC"
    )
    fun observeInRange(userId: String, startDay: Long, endDay: Long): Flow<List<ExpenseEntity>>

    @Query(
        "SELECT * FROM expenses WHERE ownerUserId = :userId AND isDeleted = 0 " +
            "AND date BETWEEN :startDay AND :endDay ORDER BY date ASC"
    )
    suspend fun getInRange(userId: String, startDay: Long, endDay: Long): List<ExpenseEntity>

    @Query(
        "SELECT COALESCE(SUM(amountCents),0) FROM expenses WHERE ownerUserId = :userId " +
            "AND isDeleted = 0 AND date BETWEEN :startDay AND :endDay"
    )
    fun observeTotalInRange(userId: String, startDay: Long, endDay: Long): Flow<Long>

    @Query(
        "SELECT expenseCategoryId, COALESCE(SUM(amountCents),0) AS total, COUNT(*) AS cnt " +
            "FROM expenses WHERE ownerUserId = :userId AND isDeleted = 0 " +
            "AND date BETWEEN :startDay AND :endDay GROUP BY expenseCategoryId ORDER BY total DESC"
    )
    fun observeCategoryTotals(userId: String, startDay: Long, endDay: Long): Flow<List<CategoryAggregate>>

    @Query(
        "SELECT merchant, COALESCE(SUM(amountCents),0) AS total, COUNT(*) AS cnt " +
            "FROM expenses WHERE ownerUserId = :userId AND isDeleted = 0 " +
            "AND date BETWEEN :startDay AND :endDay GROUP BY merchant ORDER BY total DESC LIMIT :limit"
    )
    fun observeTopVendors(userId: String, startDay: Long, endDay: Long, limit: Int): Flow<List<VendorAggregate>>

    @Query(
        "SELECT date AS epochDay, COALESCE(SUM(amountCents),0) AS amountCents FROM expenses " +
            "WHERE ownerUserId = :userId AND isDeleted = 0 AND date BETWEEN :startDay AND :endDay " +
            "GROUP BY date"
    )
    fun observeDailyTotals(userId: String, startDay: Long, endDay: Long): Flow<List<MonthAggregate>>

    @Query(
        "SELECT * FROM expenses WHERE ownerUserId = :userId AND isDeleted = 0 " +
            "AND date BETWEEN :startDay AND :endDay ORDER BY amountCents DESC LIMIT :limit"
    )
    fun observeLargest(userId: String, startDay: Long, endDay: Long, limit: Int): Flow<List<ExpenseEntity>>

    @Query(
        "SELECT * FROM expenses WHERE ownerUserId = :userId AND isDeleted = 0 " +
            "ORDER BY createdAt DESC LIMIT :limit"
    )
    fun observeRecent(userId: String, limit: Int): Flow<List<ExpenseEntity>>

    @Query(
        "SELECT COUNT(*) FROM expenses WHERE ownerUserId = :userId AND isDeleted = 0 " +
            "AND receiptLocalPath IS NULL AND receiptRemoteUrl IS NULL"
    )
    fun observeMissingReceiptCount(userId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM expenses WHERE ownerUserId = :userId AND isDeleted = 0")
    fun observeCount(userId: String): Flow<Int>

    /**
     * Flexible search. Null parameters are ignored via the `(:param IS NULL OR ...)`
     * idiom so a single query backs the whole search screen.
     */
    @Query(
        """
        SELECT * FROM expenses
        WHERE ownerUserId = :userId AND isDeleted = 0
          AND (:text IS NULL OR merchant LIKE '%' || :text || '%'
               OR notes LIKE '%' || :text || '%'
               OR businessPurpose LIKE '%' || :text || '%'
               OR tags LIKE '%' || :text || '%')
          AND (:categoryId IS NULL OR expenseCategoryId = :categoryId)
          AND (:clientId IS NULL OR clientId = :clientId)
          AND (:businessId IS NULL OR businessId = :businessId)
          AND (:fromDay IS NULL OR date >= :fromDay)
          AND (:toDay IS NULL OR date <= :toDay)
          AND (:minCents IS NULL OR amountCents >= :minCents)
          AND (:maxCents IS NULL OR amountCents <= :maxCents)
        ORDER BY date DESC, createdAt DESC
        """
    )
    fun search(
        userId: String,
        text: String?,
        categoryId: String?,
        clientId: String?,
        businessId: String?,
        fromDay: Long?,
        toDay: Long?,
        minCents: Long?,
        maxCents: Long?,
    ): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE ownerUserId = :userId AND syncStatus != 'SYNCED'")
    suspend fun getPendingSync(userId: String): List<ExpenseEntity>

    @Query("UPDATE expenses SET syncStatus = :status, remoteRowId = :remoteRowId WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus, remoteRowId: Long?)

    @Query("UPDATE expenses SET isDeleted = 1, syncStatus = 'PENDING', updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long)

    @Query("DELETE FROM expenses WHERE isDeleted = 1 AND syncStatus = 'SYNCED'")
    suspend fun purgeDeletedSynced()
}
