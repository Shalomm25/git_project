package com.ledgerly.expense.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerly.expense.core.util.DateUtils
import com.ledgerly.expense.domain.model.DashboardSummary
import com.ledgerly.expense.domain.repository.AuthRepository
import com.ledgerly.expense.domain.repository.ExpenseRepository
import com.ledgerly.expense.domain.repository.SyncInfo
import com.ledgerly.expense.domain.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val summary: DashboardSummary = DashboardSummary(),
    val syncInfo: SyncInfo = SyncInfo(),
    val isLoading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    expenseRepository: ExpenseRepository,
    private val syncRepository: SyncRepository,
) : ViewModel() {

    val summary: StateFlow<DashboardSummary> = authRepository.currentUser
        .filterNotNull()
        .flatMapLatest { user -> expenseRepository.observeDashboard(user.uid, DateUtils.today()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardSummary())

    val syncInfo: StateFlow<SyncInfo> = syncRepository.observeSyncInfo()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncInfo())

    fun syncNow() {
        viewModelScope.launch {
            val userId = runCatching { authRepository.requireUserId() }.getOrNull() ?: return@launch
            syncRepository.syncNow(userId)
        }
    }
}
