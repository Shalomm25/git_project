package com.ledgerly.expense.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerly.expense.data.security.PinManager
import com.ledgerly.expense.domain.repository.AppLockMode
import com.ledgerly.expense.domain.repository.AuthRepository
import com.ledgerly.expense.domain.repository.SettingsRepository
import com.ledgerly.expense.domain.repository.SyncInfo
import com.ledgerly.expense.domain.repository.SyncRepository
import com.ledgerly.expense.domain.repository.ThemeMode
import com.ledgerly.expense.domain.repository.UserSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val pinManager: PinManager,
) : ViewModel() {

    val settings: StateFlow<UserSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSettings())

    val syncInfo: StateFlow<SyncInfo> = syncRepository.observeSyncInfo()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SyncInfo())

    fun setTheme(mode: ThemeMode) = launch { settingsRepository.setThemeMode(mode) }
    fun setDynamicColor(enabled: Boolean) = launch { settingsRepository.setDynamicColor(enabled) }
    fun setCurrency(code: String) = launch { settingsRepository.setCurrency(code) }
    fun setTaxYear(year: Int) = launch { settingsRepository.setTaxYear(year) }
    fun setAutoSync(enabled: Boolean) = launch { settingsRepository.setAutoSync(enabled) }
    fun setWifiOnly(enabled: Boolean) = launch { settingsRepository.setSyncOnWifiOnly(enabled) }
    fun setAnalytics(enabled: Boolean) = launch { settingsRepository.setAnalyticsEnabled(enabled) }
    fun setCrashReporting(enabled: Boolean) = launch { settingsRepository.setCrashReportingEnabled(enabled) }

    fun setLockMode(mode: AppLockMode) = launch {
        if (mode == AppLockMode.NONE) pinManager.clearPin()
        settingsRepository.setLockMode(mode)
    }

    /** Sets a PIN and enables PIN lock. */
    fun setPin(pin: String) = launch {
        pinManager.setPin(pin)
        settingsRepository.setLockMode(AppLockMode.PIN)
    }

    fun signOut(onDone: () -> Unit) = launch {
        authRepository.signOut()
        onDone()
    }

    fun deleteAccount(onDone: () -> Unit) = launch {
        authRepository.deleteAccount()
        onDone()
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
