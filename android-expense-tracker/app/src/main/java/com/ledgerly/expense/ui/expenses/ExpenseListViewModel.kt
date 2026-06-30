package com.ledgerly.expense.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerly.expense.domain.model.Category
import com.ledgerly.expense.domain.model.Expense
import com.ledgerly.expense.domain.repository.AuthRepository
import com.ledgerly.expense.domain.repository.CategoryRepository
import com.ledgerly.expense.domain.repository.ExpenseQuery
import com.ledgerly.expense.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ExpenseListUiState(
    val expenses: List<Expense> = emptyList(),
    val categoriesById: Map<String, Category> = emptyMap(),
    val query: String = "",
    val isLoading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val expenseRepository: ExpenseRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    private val queryText = MutableStateFlow("")

    private val userId = authRepository.currentUser.filterNotNull()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ExpenseListUiState> = userId.flatMapLatest { user ->
        combine(
            queryText.flatMapLatest { q ->
                expenseRepository.search(user.uid, ExpenseQuery(text = q.ifBlank { null }))
            },
            categoryRepository.observeCategories(user.uid),
            queryText,
        ) { expenses, categories, q ->
            ExpenseListUiState(
                expenses = expenses,
                categoriesById = categories.associateBy { it.id },
                query = q,
                isLoading = false,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExpenseListUiState())

    fun onQueryChange(text: String) {
        queryText.value = text
    }
}
