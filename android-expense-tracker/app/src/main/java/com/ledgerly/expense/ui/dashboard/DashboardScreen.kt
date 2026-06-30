package com.ledgerly.expense.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ledgerly.expense.core.util.Money
import com.ledgerly.expense.domain.model.DashboardSummary
import com.ledgerly.expense.domain.repository.SyncState
import com.ledgerly.expense.ui.components.CategoryDonutChart
import com.ledgerly.expense.ui.components.EmptyState
import com.ledgerly.expense.ui.components.ExpenseListItem
import com.ledgerly.expense.ui.components.MonthlyTrendChart

@Composable
fun DashboardScreen(
    onExpenseClick: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val syncInfo by viewModel.syncInfo.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Overview", style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = viewModel::syncNow) {
                    Icon(
                        Icons.Outlined.Sync,
                        contentDescription = "Sync now",
                        tint = if (syncInfo.state == SyncState.SYNCING) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }

        item { TotalsRow(summary) }

        if (summary.byCategory.isNotEmpty()) {
            item {
                SectionCard("Spending by category") {
                    CategoryDonutChart(summary.byCategory)
                }
            }
        }

        if (summary.byMonth.isNotEmpty()) {
            item {
                SectionCard("Monthly trend") {
                    MonthlyTrendChart(summary.byMonth)
                }
            }
        }

        if (summary.topVendors.isNotEmpty()) {
            item {
                SectionCard("Top vendors") {
                    summary.topVendors.forEach { vendor ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(vendor.merchant, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                Money.format(vendor.totalCents),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                "Recent expenses",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        if (summary.recentExpenses.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.ReceiptLong,
                    title = "No expenses yet",
                    subtitle = "Tap + to record your first business expense.",
                )
            }
        } else {
            items(summary.recentExpenses, key = { it.id }) { expense ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    ExpenseListItem(
                        expense = expense,
                        categoryName = summary.byCategory.firstOrNull { it.categoryId == expense.expenseCategoryId }?.categoryName
                            ?: "Expense",
                        categoryColor = summary.byCategory.firstOrNull { it.categoryId == expense.expenseCategoryId }?.colorHex
                            ?: "#0E7C5A",
                        onClick = { onExpenseClick(expense.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalsRow(summary: DashboardSummary) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Today", summary.todayCents, Modifier.weight(1f))
            StatCard("This week", summary.weekCents, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("This month", summary.monthCents, Modifier.weight(1f))
            StatCard("This year", summary.yearCents, Modifier.weight(1f), highlight = true)
        }
    }
}

@Composable
private fun StatCard(label: String, cents: Long, modifier: Modifier = Modifier, highlight: Boolean = false) {
    Card(
        modifier = modifier,
        colors = if (highlight) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(
                Money.format(cents),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Column(Modifier.padding(top = 8.dp)) { content() }
        }
    }
}
