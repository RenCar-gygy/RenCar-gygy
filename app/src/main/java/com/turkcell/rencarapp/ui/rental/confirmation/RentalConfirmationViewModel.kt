package com.turkcell.rencarapp.ui.rental.confirmation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.vehicle.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RentalConfirmationViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId: String = checkNotNull(savedStateHandle["vehicleId"])

    private val _uiState = MutableStateFlow(RentalConfirmationUiState())
    val uiState: StateFlow<RentalConfirmationUiState> = _uiState.asStateFlow()

    private val _effect = Channel<RentalConfirmationEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        onIntent(RentalConfirmationIntent.LoadVehicle)
    }

    fun onIntent(intent: RentalConfirmationIntent) {
        when (intent) {
            is RentalConfirmationIntent.LoadVehicle -> loadVehicle()
            is RentalConfirmationIntent.BackClicked -> {
                viewModelScope.launch { _effect.send(RentalConfirmationEffect.NavigateBack) }
            }
            is RentalConfirmationIntent.PlanSelected -> {
                _uiState.update { 
                    val newState = it.copy(selectedPlan = intent.plan)
                    calculatePrices(newState)
                }
            }
            is RentalConfirmationIntent.TermsAcceptedChanged -> {
                _uiState.update { it.copy(isTermsAccepted = intent.accepted) }
            }
            is RentalConfirmationIntent.CompleteReservationClicked -> {
                if (_uiState.value.isTermsAccepted) {
                    viewModelScope.launch {
                        _effect.send(
                            RentalConfirmationEffect.NavigateToSummary(
                                vehicleId = vehicleId,
                                plan = _uiState.value.selectedPlan.name
                            )
                        )
                    }
                } else {
                    viewModelScope.launch {
                        _effect.send(RentalConfirmationEffect.ShowError("Lütfen kullanım şartlarını onaylayın."))
                    }
                }
            }
        }
    }

    private fun loadVehicle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            vehicleRepository.getById(vehicleId)
                .onSuccess { vehicle ->
                    _uiState.update {
                        val newState = it.copy(
                            vehicle = vehicle,
                            isLoading = false
                        )
                        calculatePrices(newState)
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Araç bilgileri alınamadı."
                        )
                    }
                    _effect.send(RentalConfirmationEffect.ShowError(exception.message ?: "Hata oluştu."))
                }
        }
    }

    private fun calculatePrices(state: RentalConfirmationUiState): RentalConfirmationUiState {
        val vehicle = state.vehicle ?: return state
        
        val dailyPrice = vehicle.pricePerDay
        val hourlyPrice = dailyPrice / 8
        val minutelyPrice = hourlyPrice / 40

        val minutelyLabel = "₺${String.format("%.2f", minutelyPrice)}/dk"
        val hourlyLabel = "₺${String.format("%.2f", hourlyPrice)}/sa"
        val dailyLabel = "₺${String.format("%.2f", dailyPrice)}"

        val baseState = state.copy(
            minutelyPriceLabel = minutelyLabel,
            hourlyPriceLabel = hourlyLabel,
            dailyPriceLabel = dailyLabel
        )

        return when (state.selectedPlan) {
            RentalPlan.MINUTELY -> {
                baseState.copy(
                    basePrice = "₺15,00",
                    estimatedDuration = "30 dk",
                    estimatedPrice = "₺${String.format("%.2f", 15.0 + (minutelyPrice * 30))}"
                )
            }
            RentalPlan.HOURLY -> {
                baseState.copy(
                    basePrice = "₺25,00",
                    estimatedDuration = "1 sa",
                    estimatedPrice = "₺${String.format("%.2f", 25.0 + hourlyPrice)}"
                )
            }
            RentalPlan.DAILY -> {
                baseState.copy(
                    basePrice = "₺0,00",
                    estimatedDuration = "1 gün",
                    estimatedPrice = "₺${String.format("%.2f", dailyPrice)}"
                )
            }
        }
    }
}
