package com.ledgerly.expense.domain.repository

import kotlinx.coroutines.flow.Flow

enum class ThemeMode { LIGHT, DARK, SYSTEM }

enum class AppLockMode { NONE, PIN, BIOMETRIC }

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val currencyCode: String = "USD",
    val taxYear: Int = 2026,
    val lockMode: AppLockMode = AppLockMode.NONE,
    val autoLockMinutes: Int = 1,
    val autoSyncEnabled: Boolean = true,
    val syncOnWifiOnly: Boolean = false,
    val analyticsEnabled: Boolean = false,
    val crashReportingEnabled: Boolean = false,
    val mileageRateCents: Long = 67, // 2024 IRS standard mileage rate ($0.67/mi)
    val spreadsheetId: String? = null,
    val onboardingComplete: Boolean = false,
)

/** Persists non-sensitive preferences (DataStore) and exposes them reactively. */
interface SettingsRepository {
    val settings: Flow<UserSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setDynamicColor(enabled: Boolean)
    suspend fun setCurrency(code: String)
    suspend fun setTaxYear(year: Int)
    suspend fun setLockMode(mode: AppLockMode)
    suspend fun setAutoLockMinutes(minutes: Int)
    suspend fun setAutoSync(enabled: Boolean)
    suspend fun setSyncOnWifiOnly(enabled: Boolean)
    suspend fun setAnalyticsEnabled(enabled: Boolean)
    suspend fun setCrashReportingEnabled(enabled: Boolean)
    suspend fun setMileageRateCents(cents: Long)
    suspend fun setSpreadsheetId(id: String?)
    suspend fun setOnboardingComplete(complete: Boolean)
}
