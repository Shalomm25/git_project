package com.ledgerly.expense.data

import com.google.common.truth.Truth.assertThat
import com.ledgerly.expense.data.local.toDomain
import com.ledgerly.expense.data.local.toEntity
import com.ledgerly.expense.domain.model.Expense
import com.ledgerly.expense.domain.model.SyncStatus
import org.junit.Test
import java.time.LocalDate

class ExpenseMapperTest {

    private val sample = Expense(
        id = "abc",
        date = LocalDate.of(2026, 3, 14),
        merchant = "Staples",
        amountCents = 4999,
        expenseCategoryId = "cat-1",
        scheduleCLine = "22",
        paymentMethod = "Business Credit Card",
        businessPurpose = "Printer paper",
        notes = null,
        tags = listOf("office", "supplies"),
        syncStatus = SyncStatus.PENDING,
        createdAt = 1_700_000_000_000,
        updatedAt = 1_700_000_000_000,
        ownerUserId = "user-1",
    )

    @Test
    fun `entity round-trip preserves all fields`() {
        val restored = sample.toEntity().toDomain()
        assertThat(restored).isEqualTo(sample)
    }

    @Test
    fun `tags serialize to csv and back`() {
        val entity = sample.toEntity()
        assertThat(entity.tags).isEqualTo("office,supplies")
        assertThat(entity.toDomain().tags).containsExactly("office", "supplies").inOrder()
    }

    @Test
    fun `date stored as epoch day`() {
        assertThat(sample.toEntity().date).isEqualTo(LocalDate.of(2026, 3, 14).toEpochDay())
    }

    @Test
    fun `empty tags map to null and back to empty list`() {
        val noTags = sample.copy(tags = emptyList())
        assertThat(noTags.toEntity().tags).isNull()
        assertThat(noTags.toEntity().toDomain().tags).isEmpty()
    }
}
