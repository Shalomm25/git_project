package com.ledgerly.expense.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/** Type-safe route constants for the app's navigation graph. */
object Routes {
    const val DASHBOARD = "dashboard"
    const val EXPENSES = "expenses"
    const val REPORTS = "reports"
    const val SETTINGS = "settings"
    const val SEARCH = "search"
    const val MILEAGE = "mileage"
    const val CATEGORIES = "categories"

    const val EXPENSE_ENTRY = "expense_entry"
    const val EXPENSE_ENTRY_ARG_ID = "expenseId"
    fun expenseEntry(expenseId: String? = null): String =
        if (expenseId == null) "$EXPENSE_ENTRY?$EXPENSE_ENTRY_ARG_ID=" else "$EXPENSE_ENTRY?$EXPENSE_ENTRY_ARG_ID=$expenseId"
}

/** Bottom navigation items. */
enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    DASHBOARD(Routes.DASHBOARD, "Dashboard", Icons.Outlined.Dashboard),
    EXPENSES(Routes.EXPENSES, "Expenses", Icons.Outlined.ReceiptLong),
    REPORTS(Routes.REPORTS, "Reports", Icons.Outlined.BarChart),
    SETTINGS(Routes.SETTINGS, "Settings", Icons.Outlined.Settings),
}
