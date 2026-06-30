package com.ledgerly.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerly.expense.domain.repository.AuthRepository
import com.ledgerly.expense.domain.repository.AuthUser
import com.ledgerly.expense.domain.repository.CategoryRepository
import com.ledgerly.expense.domain.repository.PaymentMethodRepository
import com.ledgerly.expense.domain.repository.SettingsRepository
import com.ledgerly.expense.domain.repository.UserSettings
import com.ledgerly.expense.security.AppLockManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthState {
    data object Loading : AuthState
    data object SignedOut : AuthState
    data class SignedIn(val user: AuthUser) : AuthState
}

data class MainUiState(
    val settings: UserSettings = UserSettings(),
    val authState: AuthState = AuthState.Loading,
    val isLocked: Boolean = false,
)

/**
 * Top-level state holder driving theme, the auth gate, and the app-lock gate.
 * On sign-in it seeds per-user defaults (categories, payment methods) so a new
 * account is immediately usable.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    val appLockManager: AppLockManager,
) : ViewModel() {

    private val authState: StateFlow<AuthState> = authRepository.currentUser
        .onEach { user -> if (user != null) onSignedIn(user) }
        .map { user -> if (user == null) AuthState.SignedOut else AuthState.SignedIn(user) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AuthState.Loading)

    val uiState: StateFlow<MainUiState> = combine(
        settingsRepository.settings,
        authState,
        appLockManager.isLocked,
    ) { settings, auth, locked ->
        appLockManager.configure(settings.lockMode, settings.autoLockMinutes)
        MainUiState(settings = settings, authState = auth, isLocked = locked)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MainUiState())

    private fun onSignedIn(user: AuthUser) {
        viewModelScope.launch {
            categoryRepository.seedDefaultsIfEmpty(user.uid)
            paymentMethodRepository.seedDefaultsIfEmpty(user.uid)
        }
    }
}
