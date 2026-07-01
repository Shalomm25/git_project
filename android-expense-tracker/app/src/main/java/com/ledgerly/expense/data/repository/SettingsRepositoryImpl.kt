package com.ledgerly.expense.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ledgerly.expense.domain.repository.AppLockMode
import com.ledgerly.expense.domain.repository.SettingsRepository
import com.ledgerly.expense.domain.repository.ThemeMode
import com.ledgerly.expense.domain.repository.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ledgerly_settings")

/** Persists user preferences in DataStore and exposes them as a typed flow. */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsRepository {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val DYNAMIC = booleanPreferencesKey("dynamic_color")
        val CURRENCY = stringPreferencesKey("currency")
        val TAX_YEAR = intPreferencesKey("tax_year")
        val LOCK = stringPreferencesKey("lock_mode")
        val AUTO_LOCK = intPreferencesKey("auto_lock_minutes")
        val AUTO_SYNC = booleanPreferencesKey("auto_sync")
        val WIFI_ONLY = booleanPreferencesKey("sync_wifi_only")
        val ANALYTICS = booleanPreferencesKey("analytics")
        val CRASH = booleanPreferencesKey("crash_reporting")
        val MILEAGE_RATE = longPreferencesKey("mileage_rate_cents")
        val SPREADSHEET = stringPreferencesKey("spreadsheet_id")
        val ONBOARDING = booleanPreferencesKey("onboarding_complete")
    }

    override val settings: Flow<UserSettings> = context.dataStore.data.map { p ->
        val defaults = UserSettings()
        UserSettings(
            themeMode = p[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: defaults.themeMode,
            dynamicColor = p[Keys.DYNAMIC] ?: defaults.dynamicColor,
            currencyCode = p[Keys.CURRENCY] ?: defaults.currencyCode,
            taxYear = p[Keys.TAX_YEAR] ?: defaults.taxYear,
            lockMode = p[Keys.LOCK]?.let { runCatching { AppLockMode.valueOf(it) }.getOrNull() }
                ?: defaults.lockMode,
            autoLockMinutes = p[Keys.AUTO_LOCK] ?: defaults.autoLockMinutes,
            autoSyncEnabled = p[Keys.AUTO_SYNC] ?: defaults.autoSyncEnabled,
            syncOnWifiOnly = p[Keys.WIFI_ONLY] ?: defaults.syncOnWifiOnly,
            analyticsEnabled = p[Keys.ANALYTICS] ?: defaults.analyticsEnabled,
            crashReportingEnabled = p[Keys.CRASH] ?: defaults.crashReportingEnabled,
            mileageRateCents = p[Keys.MILEAGE_RATE] ?: defaults.mileageRateCents,
            spreadsheetId = p[Keys.SPREADSHEET],
            onboardingComplete = p[Keys.ONBOARDING] ?: defaults.onboardingComplete,
        )
    }

    private suspend fun edit(block: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        context.dataStore.edit(block)
    }

    override suspend fun setThemeMode(mode: ThemeMode) = edit { it[Keys.THEME] = mode.name }
    override suspend fun setDynamicColor(enabled: Boolean) = edit { it[Keys.DYNAMIC] = enabled }
    override suspend fun setCurrency(code: String) = edit { it[Keys.CURRENCY] = code }
    override suspend fun setTaxYear(year: Int) = edit { it[Keys.TAX_YEAR] = year }
    override suspend fun setLockMode(mode: AppLockMode) = edit { it[Keys.LOCK] = mode.name }
    override suspend fun setAutoLockMinutes(minutes: Int) = edit { it[Keys.AUTO_LOCK] = minutes }
    override suspend fun setAutoSync(enabled: Boolean) = edit { it[Keys.AUTO_SYNC] = enabled }
    override suspend fun setSyncOnWifiOnly(enabled: Boolean) = edit { it[Keys.WIFI_ONLY] = enabled }
    override suspend fun setAnalyticsEnabled(enabled: Boolean) = edit { it[Keys.ANALYTICS] = enabled }
    override suspend fun setCrashReportingEnabled(enabled: Boolean) = edit { it[Keys.CRASH] = enabled }
    override suspend fun setMileageRateCents(cents: Long) = edit { it[Keys.MILEAGE_RATE] = cents }
    override suspend fun setSpreadsheetId(id: String?) = edit {
        if (id == null) it.remove(Keys.SPREADSHEET) else it[Keys.SPREADSHEET] = id
    }
    override suspend fun setOnboardingComplete(complete: Boolean) =
        edit { it[Keys.ONBOARDING] = complete }
}
