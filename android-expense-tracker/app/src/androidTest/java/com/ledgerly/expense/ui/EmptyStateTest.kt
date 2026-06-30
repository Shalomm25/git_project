package com.ledgerly.expense.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.ledgerly.expense.ui.components.EmptyState
import com.ledgerly.expense.ui.theme.LedgerlyTheme
import org.junit.Rule
import org.junit.Test

class EmptyStateTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun emptyState_showsTitleAndSubtitle() {
        composeRule.setContent {
            LedgerlyTheme {
                EmptyState(
                    icon = Icons.Outlined.ReceiptLong,
                    title = "No expenses yet",
                    subtitle = "Tap + to record your first business expense.",
                )
            }
        }

        composeRule.onNodeWithText("No expenses yet").assertIsDisplayed()
        composeRule.onNodeWithText("Tap + to record your first business expense.").assertIsDisplayed()
    }
}
