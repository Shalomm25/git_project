package com.ledgerly.expense.ui.reports

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerly.expense.data.export.ReportExporter
import com.ledgerly.expense.domain.model.ExportFormat
import com.ledgerly.expense.domain.model.ScheduleCReport
import com.ledgerly.expense.domain.repository.AuthRepository
import com.ledgerly.expense.domain.repository.ReportRepository
import com.ledgerly.expense.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportsUiState(
    val taxYear: Int = 2026,
    val report: ScheduleCReport? = null,
    val isLoading: Boolean = true,
    val isExporting: Boolean = false,
    val exportError: String? = null,
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val reportRepository: ReportRepository,
    private val settingsRepository: SettingsRepository,
    private val reportExporter: ReportExporter,
) : ViewModel() {

    private val _state = MutableStateFlow(ReportsUiState())
    val state: StateFlow<ReportsUiState> = _state.asStateFlow()

    /** One-shot share intents emitted after a successful export. */
    private val _shareEvents = Channel<Intent>(Channel.BUFFERED)
    val shareEvents = _shareEvents.receiveAsFlow()

    fun export(format: ExportFormat) {
        viewModelScope.launch {
            val userId = runCatching { authRepository.requireUserId() }.getOrNull() ?: return@launch
            _state.value = _state.value.copy(isExporting = true, exportError = null)
            runCatching { reportExporter.export(userId, _state.value.taxYear, format) }
                .onSuccess { file ->
                    _state.value = _state.value.copy(isExporting = false)
                    _shareEvents.send(reportExporter.shareIntent(file))
                }
                .onFailure { t ->
                    _state.value = _state.value.copy(isExporting = false, exportError = t.message)
                }
        }
    }

    init {
        viewModelScope.launch {
            val year = settingsRepository.settings.first().taxYear
            _state.value = _state.value.copy(taxYear = year)
            load(year)
        }
    }

    fun selectYear(year: Int) {
        _state.value = _state.value.copy(taxYear = year)
        load(year)
    }

    private fun load(year: Int) {
        viewModelScope.launch {
            val userId = runCatching { authRepository.requireUserId() }.getOrNull() ?: return@launch
            _state.value = _state.value.copy(isLoading = true)
            val report = reportRepository.buildScheduleCReport(userId, year)
            _state.value = _state.value.copy(report = report, isLoading = false)
        }
    }
}
