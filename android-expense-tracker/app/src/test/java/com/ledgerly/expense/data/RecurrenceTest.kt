package com.ledgerly.expense.data

import com.google.common.truth.Truth.assertThat
import com.ledgerly.expense.data.repository.advance
import com.ledgerly.expense.domain.model.RecurrenceFrequency
import org.junit.Test
import java.time.LocalDate

class RecurrenceTest {

    private val base = LocalDate.of(2026, 1, 31)

    @Test
    fun `weekly advances seven days`() {
        assertThat(RecurrenceFrequency.WEEKLY.advance(base)).isEqualTo(LocalDate.of(2026, 2, 7))
    }

    @Test
    fun `monthly clamps end of month`() {
        // Jan 31 + 1 month -> Feb 28 (2026 is not a leap year)
        assertThat(RecurrenceFrequency.MONTHLY.advance(base)).isEqualTo(LocalDate.of(2026, 2, 28))
    }

    @Test
    fun `quarterly advances three months`() {
        assertThat(RecurrenceFrequency.QUARTERLY.advance(LocalDate.of(2026, 1, 15)))
            .isEqualTo(LocalDate.of(2026, 4, 15))
    }

    @Test
    fun `yearly advances one year`() {
        assertThat(RecurrenceFrequency.YEARLY.advance(LocalDate.of(2026, 6, 30)))
            .isEqualTo(LocalDate.of(2027, 6, 30))
    }
}
