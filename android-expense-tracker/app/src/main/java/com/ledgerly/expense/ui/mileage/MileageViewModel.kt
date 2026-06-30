package com.ledgerly.expense.ui.mileage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ledgerly.expense.core.util.DateUtils
import com.ledgerly.expense.domain.model.MileageTrip
import com.ledgerly.expense.domain.repository.AuthRepository
import com.ledgerly.expense.domain.repository.MileageRepository
import com.ledgerly.expense.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MileageViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val mileageRepository: MileageRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val trips: StateFlow<List<MileageTrip>> = authRepository.currentUser
        .filterNotNull()
        .flatMapLatest { mileageRepository.observeTrips(it.uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addTrip(start: String, end: String, miles: Double, purpose: String) {
        viewModelScope.launch {
            val userId = runCatching { authRepository.requireUserId() }.getOrNull() ?: return@launch
            val rate = settingsRepository.settings.first().mileageRateCents
            mileageRepository.upsert(
                MileageTrip(
                    id = UUID.randomUUID().toString(),
                    date = DateUtils.today(),
                    startLocation = start,
                    endLocation = end,
                    totalMiles = miles,
                    businessPurpose = purpose.ifBlank { null },
                    ratePerMileCents = rate,
                    ownerUserId = userId,
                ),
            )
        }
    }

    fun delete(trip: MileageTrip) {
        viewModelScope.launch { mileageRepository.delete(trip.id) }
    }
}
