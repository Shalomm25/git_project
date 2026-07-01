package com.ledgerly.expense.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerly.expense.domain.model.Category
import com.ledgerly.expense.domain.model.ScheduleCCategory
import com.ledgerly.expense.domain.repository.AuthRepository
import com.ledgerly.expense.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    val categories: StateFlow<List<Category>> = authRepository.currentUser
        .filterNotNull()
        .flatMapLatest { categoryRepository.observeCategories(it.uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** All IRS Schedule C lines a user can map a custom category to. */
    val scheduleCOptions: List<ScheduleCCategory> = ScheduleCCategory.entries

    fun addOrUpdate(existing: Category?, name: String, scheduleCLine: String, colorHex: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val userId = runCatching { authRepository.requireUserId() }.getOrNull() ?: return@launch
            val category = existing?.copy(name = name, scheduleCLine = scheduleCLine, colorHex = colorHex)
                ?: Category(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    scheduleCLine = scheduleCLine,
                    colorHex = colorHex,
                    isDefault = false,
                    sortOrder = categories.value.size,
                    ownerUserId = userId,
                )
            categoryRepository.upsert(category)
        }
    }

    fun delete(category: Category) {
        viewModelScope.launch { categoryRepository.delete(category.id) }
    }
}
