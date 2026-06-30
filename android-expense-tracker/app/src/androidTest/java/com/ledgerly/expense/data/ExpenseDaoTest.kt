package com.ledgerly.expense.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.ledgerly.expense.data.local.LedgerlyDatabase
import com.ledgerly.expense.data.local.dao.ExpenseDao
import com.ledgerly.expense.data.local.entity.ExpenseEntity
import com.ledgerly.expense.domain.model.SyncStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class ExpenseDaoTest {

    private lateinit var db: LedgerlyDatabase
    private lateinit var dao: ExpenseDao
    private val user = "user-1"

    @Before
    fun setup() {
        // In-memory DB (no SQLCipher) for fast, isolated DAO tests.
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            LedgerlyDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.expenseDao()
    }

    @After
    fun teardown() = db.close()

    private fun expense(id: String, day: LocalDate, cents: Long) = ExpenseEntity(
        id = id, ownerUserId = user, date = day.toEpochDay(), merchant = "M-$id",
        amountCents = cents, currency = "USD", expenseCategoryId = "cat", scheduleCLine = "22",
        paymentMethod = null, businessPurpose = null, notes = null, receiptLocalPath = null,
        receiptRemoteUrl = null, mileage = null, latitude = null, longitude = null, tags = null,
        clientId = null, businessId = null, recurringId = null, syncStatus = SyncStatus.PENDING,
        remoteRowId = null, createdAt = 0, updatedAt = 0, isDeleted = false,
    )

    @Test
    fun insertAndObserveTotalInRange() = runTest {
        val today = LocalDate.of(2026, 6, 1)
        dao.upsert(expense("a", today, 1000))
        dao.upsert(expense("b", today, 2500))
        dao.upsert(expense("c", today.minusMonths(2), 9999))

        val total = dao.observeTotalInRange(user, today.toEpochDay(), today.toEpochDay()).first()
        assertThat(total).isEqualTo(3500)
    }

    @Test
    fun softDeleteHidesFromQueries() = runBlocking {
        val today = LocalDate.of(2026, 6, 1)
        dao.upsert(expense("a", today, 1000))
        dao.softDelete("a", now = 1)

        assertThat(dao.getById("a")).isNull()
        assertThat(dao.observeAll(user).first()).isEmpty()
        // Still present as a tombstone for sync until purged.
        assertThat(dao.getPendingSync(user).any { it.id == "a" }).isTrue()
    }

    @Test
    fun searchFiltersByMerchantText() = runTest {
        val today = LocalDate.of(2026, 6, 1)
        dao.upsert(expense("a", today, 1000).copy(merchant = "Starbucks"))
        dao.upsert(expense("b", today, 1000).copy(merchant = "Office Depot"))

        val results = dao.search(user, "Office", null, null, null, null, null, null, null).first()
        assertThat(results.map { it.id }).containsExactly("b")
    }
}
