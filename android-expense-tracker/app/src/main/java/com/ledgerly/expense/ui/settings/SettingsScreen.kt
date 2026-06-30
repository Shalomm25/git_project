package com.ledgerly.expense.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ledgerly.expense.domain.repository.AppLockMode
import com.ledgerly.expense.domain.repository.ThemeMode

@Composable
fun SettingsScreen(
    onSignedOut: () -> Unit,
    onManageCategories: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val syncInfo by viewModel.syncInfo.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)

        SettingsSection("Appearance") {
            Text("Theme", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = settings.themeMode == mode,
                        onClick = { viewModel.setTheme(mode) },
                        label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }
            ToggleRow("Material You colors", settings.dynamicColor, viewModel::setDynamicColor)
        }

        SettingsSection("Sync & data") {
            Text(
                syncInfo.spreadsheetUrl?.let { "Connected to Google Sheets" }
                    ?: "Not yet synced to Google Sheets",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ToggleRow("Automatic background sync", settings.autoSyncEnabled, viewModel::setAutoSync)
            ToggleRow("Sync on Wi-Fi only", settings.syncOnWifiOnly, viewModel::setWifiOnly)
        }

        SettingsSection("Security") {
            Text("App lock", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppLockMode.entries.forEach { mode ->
                    FilterChip(
                        selected = settings.lockMode == mode,
                        onClick = { viewModel.setLockMode(mode) },
                        label = {
                            Text(
                                when (mode) {
                                    AppLockMode.NONE -> "Off"
                                    AppLockMode.PIN -> "PIN"
                                    AppLockMode.BIOMETRIC -> "Biometric"
                                },
                            )
                        },
                    )
                }
            }
            Text(
                "Your data is encrypted on-device with a hardware-backed key.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SettingsSection("Tax") {
            Text("Tax year", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(settings.taxYear - 1, settings.taxYear, settings.taxYear + 1).forEach { year ->
                    FilterChip(
                        selected = settings.taxYear == year,
                        onClick = { viewModel.setTaxYear(year) },
                        label = { Text(year.toString()) },
                    )
                }
            }
            TextButton(onClick = onManageCategories) { Text("Manage categories") }
        }

        SettingsSection("Privacy") {
            ToggleRow("Crash reporting", settings.crashReportingEnabled, viewModel::setCrashReporting)
            ToggleRow("Usage analytics", settings.analyticsEnabled, viewModel::setAnalytics)
        }

        SettingsSection("Account") {
            TextButton(onClick = { viewModel.signOut(onSignedOut) }) { Text("Sign out") }
            TextButton(onClick = { viewModel.deleteAccount(onSignedOut) }) {
                Text("Delete account", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Divider()
            content()
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
