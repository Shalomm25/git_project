package com.ledgerly.expense.ui.expenses

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ledgerly.expense.ui.components.EmptyState
import com.ledgerly.expense.ui.components.ExpenseListItem

@Composable
fun ExpenseListScreen(
    onExpenseClick: (String) -> Unit,
    viewModel: ExpenseListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
    ) {
        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                placeholder = { Text("Search merchant, notes, tags…") },
            )
        }

        if (state.expenses.isEmpty() && !state.isLoading) {
            item {
                EmptyState(
                    icon = Icons.Outlined.ReceiptLong,
                    title = if (state.query.isBlank()) "No expenses yet" else "No matches",
                    subtitle = if (state.query.isBlank()) {
                        "Your recorded expenses will appear here."
                    } else {
                        "Try a different search term."
                    },
                )
            }
        } else {
            items(state.expenses, key = { it.id }) { expense ->
                val category = state.categoriesById[expense.expenseCategoryId]
                ExpenseListItem(
                    expense = expense,
                    categoryName = category?.name ?: "Expense",
                    categoryColor = category?.colorHex ?: "#0E7C5A",
                    onClick = { onExpenseClick(expense.id) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
