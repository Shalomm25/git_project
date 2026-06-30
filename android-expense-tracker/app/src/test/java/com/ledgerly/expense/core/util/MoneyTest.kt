package com.ledgerly.expense.core.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class MoneyTest {

    @Test
    fun `parseToCents handles decimals and currency symbols`() {
        assertThat(Money.parseToCents("12.50")).isEqualTo(1250)
        assertThat(Money.parseToCents("$1,234.00")).isEqualTo(123400)
        assertThat(Money.parseToCents("0.99")).isEqualTo(99)
    }

    @Test
    fun `parseToCents rounds to nearest cent`() {
        assertThat(Money.parseToCents("9.994")).isEqualTo(999) // 999.4 -> 999
        assertThat(Money.parseToCents("9.996")).isEqualTo(1000) // 999.6 -> 1000
    }

    @Test
    fun `parseToCents returns null for invalid input`() {
        assertThat(Money.parseToCents("")).isNull()
        assertThat(Money.parseToCents("abc")).isNull()
    }

    @Test
    fun `centsToPlainString always has two decimals`() {
        assertThat(Money.centsToPlainString(1250)).isEqualTo("12.50")
        assertThat(Money.centsToPlainString(5)).isEqualTo("0.05")
        assertThat(Money.centsToPlainString(0)).isEqualTo("0.00")
    }

    @Test
    fun `format produces USD currency string`() {
        assertThat(Money.format(123456, "USD")).isEqualTo("$1,234.56")
    }

    @Test
    fun `formatCompact abbreviates large amounts`() {
        assertThat(Money.formatCompact(150000)).isEqualTo("$1.5K")
        assertThat(Money.formatCompact(250000000)).isEqualTo("$2.5M")
    }
}
