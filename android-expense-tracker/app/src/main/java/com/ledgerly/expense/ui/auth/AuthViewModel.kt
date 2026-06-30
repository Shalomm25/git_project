package com.ledgerly.expense.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerly.expense.core.AppResult
import com.ledgerly.expense.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isRegistering: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) { _state.value = _state.value.copy(email = value) }
    fun onPasswordChange(value: String) { _state.value = _state.value.copy(password = value) }
    fun toggleMode() { _state.value = _state.value.copy(isRegistering = !_state.value.isRegistering, error = null) }

    fun submitEmail() {
        val s = _state.value
        runAuth {
            if (s.isRegistering) {
                authRepository.registerWithEmail(s.email.trim(), s.password)
            } else {
                authRepository.signInWithEmail(s.email.trim(), s.password)
            }
        }
    }

    fun signInWithGoogle(idToken: String) = runAuth { authRepository.signInWithGoogle(idToken) }

    fun continueAsGuest() = runAuth { authRepository.continueAsGuest() }

    private fun runAuth(block: suspend () -> AppResult<*>) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = block()) {
                is AppResult.Success -> Unit // auth state flow drives navigation
                is AppResult.Error ->
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
            }
        }
    }
}
