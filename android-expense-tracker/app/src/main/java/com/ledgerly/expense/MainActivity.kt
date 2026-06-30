package com.ledgerly.expense

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.ledgerly.expense.domain.repository.AppLockMode
import com.ledgerly.expense.security.BiometricAuthenticator
import com.ledgerly.expense.security.BiometricResult
import com.ledgerly.expense.ui.auth.AuthScreen
import com.ledgerly.expense.ui.components.LoadingState
import com.ledgerly.expense.ui.navigation.LedgerlyNavHost
import com.ledgerly.expense.ui.security.LockScreen
import com.ledgerly.expense.ui.theme.LedgerlyTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Single-activity host. Extends [FragmentActivity] so AndroidX BiometricPrompt
 * can attach. Renders the theme, then routes between the auth gate, the app-lock
 * gate, and the main navigation graph based on [MainViewModel] state.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()
    private val biometricAuthenticator by lazy { BiometricAuthenticator(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        viewModel.appLockManager.onAppStart()

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            LedgerlyTheme(
                themeMode = state.settings.themeMode,
                dynamicColor = state.settings.dynamicColor,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    when (val auth = state.authState) {
                        AuthState.Loading -> LoadingState()
                        AuthState.SignedOut -> AuthScreen()
                        is AuthState.SignedIn -> {
                            if (state.isLocked) {
                                LockScreen(
                                    lockMode = state.settings.lockMode,
                                    onBiometricRequest = ::promptBiometric,
                                    onUnlocked = viewModel.appLockManager::unlock,
                                )
                                // Auto-prompt biometrics once when the lock appears.
                                LaunchedEffect(auth.user.uid) {
                                    if (state.settings.lockMode == AppLockMode.BIOMETRIC) promptBiometric()
                                }
                            } else {
                                LedgerlyNavHost(onSignedOut = { /* auth flow handles it */ })
                            }
                        }
                    }
                }
            }
        }
    }

    private fun promptBiometric() {
        lifecycleScope.launch {
            val result = biometricAuthenticator.authenticate(
                title = getString(R.string.unlock_biometric_prompt),
                subtitle = getString(R.string.unlock_biometric_subtitle),
            )
            if (result is BiometricResult.Success) viewModel.appLockManager.unlock()
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.appLockManager.onEnterBackground()
    }

    override fun onStart() {
        super.onStart()
        viewModel.appLockManager.onEnterForeground()
    }
}
