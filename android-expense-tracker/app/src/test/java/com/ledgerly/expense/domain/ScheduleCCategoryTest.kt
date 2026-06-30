package com.ledgerly.expense.domain

import com.google.common.truth.Truth.assertThat
import com.ledgerly.expense.data.local.DefaultData
import com.ledgerly.expense.domain.model.ScheduleCCategory
import org.junit.Test

class ScheduleCCategoryTest {

    @Test
    fun `fromLine round-trips every category`() {
        ScheduleCCategory.entries.forEach { category ->
            assertThat(ScheduleCCategory.fromLine(category.line)).isEqualTo(category)
        }
    }

    @Test
    fun `fromLine returns null for unknown line`() {
        assertThat(ScheduleCCategory.fromLine("999")).isNull()
    }

    @Test
    fun `every default category maps to a valid Schedule C line`() {
        val defaults = DefaultData.defaultCategories(userId = "test-user")
        assertThat(defaults).isNotEmpty()
        defaults.forEach { category ->
            assertThat(ScheduleCCategory.fromLine(category.scheduleCLine)).isNotNull()
        }
    }

    @Test
    fun `business meals map to deductible meals line 24b`() {
        val meals = DefaultData.defaultCategories("u").first { it.name == "Business Meals" }
        assertThat(meals.scheduleCLine).isEqualTo(ScheduleCCategory.MEALS.line)
        assertThat(ScheduleCCategory.MEALS.line).isEqualTo("24b")
    }
}
