package com.turkcell.rencarapp.ui.rental.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.rental.CreateRentalRequest
import com.turkcell.rencarapp.data.rental.RentalRepository
import com.turkcell.rencarapp.data.vehicle.VehicleRepository
import com.turkcell.rencarapp.ui.navigation.RenCarDestination
import com.turkcell.rencarapp.ui.rental.confirmation.RentalPlan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class RentalSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vehicleRepository: VehicleRepository,
    private val rentalRepository: RentalRepository
) : ViewModel() {

    private val vehicleId: String = checkNotNull(savedStateHandle[RenCarDestination.ARG_VEHICLE_ID])
    private val plan: String? = savedStateHandle[RenCarDestination.ARG_PLAN]

    private val _uiState = MutableStateFlow(RentalSummaryUiState(vehicleId = vehicleId, selectedPlan = plan))
    val uiState: StateFlow<RentalSummaryUiState> = _uiState.asStateFlow()

    private val _effect = Channel<RentalSummaryEffect>(Channel.BUFFERED)
    val effect: Flow<RentalSummaryEffect> = _effect.receiveAsFlow()

    init {
        loadVehicleDetails()
    }

    fun onIntent(intent: RentalSummaryIntent) {
        when (intent) {
            is RentalSummaryIntent.PayClicked -> createRental()
            is RentalSummaryIntent.ChangeCardClicked -> {
                viewModelScope.launch {
                    _effect.send(RentalSummaryEffect.ShowToast("Kart değiştirme henüz aktif değil (Stub)"))
                }
            }
        }
    }

    @android.annotation.SuppressLint("NewApi")
    private fun createRental() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val endDate = calculateEndDate(plan)
            val request = CreateRentalRequest(vehicleId, endDate)

            rentalRepository.create(request)
                .onSuccess { rental ->
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(RentalSummaryEffect.NavigateToDeliveryPhotos(rental.id))
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(RentalSummaryEffect.ShowError(error.message ?: "Kiralama oluşturulamadı."))
                }
        }
    }

    @android.annotation.SuppressLint("NewApi")
    private fun calculateEndDate(planString: String?): Instant {
        val now = Instant.now()
        return when (planString) {
            RentalPlan.MINUTELY.name -> now.plus(30, ChronoUnit.MINUTES)
            RentalPlan.HOURLY.name -> now.plus(1, ChronoUnit.HOURS)
            RentalPlan.DAILY.name -> now.plus(1, ChronoUnit.DAYS)
            else -> now.plus(1, ChronoUnit.HOURS)
        }
    }

    private fun loadVehicleDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            vehicleRepository.getById(vehicleId)
                .onSuccess { vehicle ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            vehicleName = "${vehicle.brand} ${vehicle.model}",
                            plate = vehicle.plate
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(RentalSummaryEffect.ShowError(error.message ?: "Araç bilgileri alınamadı."))
                }
        }
    }
}