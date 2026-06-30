package com.ledgerly.expense.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ledgerly.expense.ui.categories.CategoriesScreen
import com.ledgerly.expense.ui.dashboard.DashboardScreen
import com.ledgerly.expense.ui.expenses.ExpenseEntryScreen
import com.ledgerly.expense.ui.expenses.ExpenseListScreen
import com.ledgerly.expense.ui.mileage.MileageScreen
import com.ledgerly.expense.ui.reports.ReportsScreen
import com.ledgerly.expense.ui.settings.SettingsScreen

/**
 * Hosts the main app navigation: a bottom bar for the four top-level
 * destinations, a global FAB for one-tap expense entry, and modal-style entry/
 * detail destinations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerlyNavHost(onSignedOut: () -> Unit) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val isTopLevel = TopLevelDestination.entries.any { it.route == currentRoute }
    val showFab = currentRoute in setOf(Routes.DASHBOARD, Routes.EXPENSES)

    Scaffold(
        topBar = {
            val title = TopLevelDestination.entries.firstOrNull { it.route == currentRoute }?.label
                ?: "Ledgerly"
            if (isTopLevel) TopAppBar(title = { Text(title) })
        },
        bottomBar = {
            if (isTopLevel) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { dest ->
                        val selected = backStackEntry?.destination?.hierarchy
                            ?.any { it.route == dest.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(onClick = { navController.navigate(Routes.expenseEntry()) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add expense")
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(tween(220)) },
            exitTransition = { fadeOut(tween(180)) },
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(onExpenseClick = { navController.navigate(Routes.expenseEntry(it)) })
            }
            composable(Routes.EXPENSES) {
                ExpenseListScreen(onExpenseClick = { navController.navigate(Routes.expenseEntry(it)) })
            }
            composable(Routes.REPORTS) { ReportsScreen() }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onSignedOut = onSignedOut,
                    onManageCategories = { navController.navigate(Routes.CATEGORIES) },
                )
            }
            composable(Routes.CATEGORIES) { CategoriesScreen(onBack = navController::popBackStack) }
            composable(Routes.MILEAGE) { MileageScreen(onBack = navController::popBackStack) }
            composable(
                route = "${Routes.EXPENSE_ENTRY}?${Routes.EXPENSE_ENTRY_ARG_ID}={${Routes.EXPENSE_ENTRY_ARG_ID}}",
                arguments = listOf(
                    navArgument(Routes.EXPENSE_ENTRY_ARG_ID) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
            ) {
                ExpenseEntryScreen(
                    onSaved = navController::popBackStack,
                    onCancel = navController::popBackStack,
                )
            }
        }
    }
}
