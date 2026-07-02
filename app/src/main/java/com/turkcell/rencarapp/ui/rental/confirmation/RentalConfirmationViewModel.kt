package com.turkcell.rencarapp.ui.rental.confirmation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.network.dto.VehicleResponseDto
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
                _uiState.update { it.copy(selectedPlan = intent.plan) }
            }
            is RentalConfirmationIntent.TermsAcceptedChanged -> {
                _uiState.update { it.copy(isTermsAccepted = intent.accepted) }
            }
            is RentalConfirmationIntent.CompleteReservationClicked -> {
                if (_uiState.value.isTermsAccepted) {
                    viewModelScope.launch {
                        _effect.send(RentalConfirmationEffect.NavigateToSummary(vehicleId))
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
            _uiState.update { it.copy(isLoading = true) }
            
            // Fake veri
            val fakeVehicle = VehicleResponseDto(
                id = vehicleId,
                plate = "34 RNC 022",
                brand = "Renault",
                model = "Clio",
                type = "MÜSAİT",
                pricePerDay = 180.0,
                status = "AVAILABLE",
                latitude = 41.0,
                longitude = 29.0
            )
            
            _uiState.update { 
                it.copy(
                    vehicle = fakeVehicle, 
                    isLoading = false
                ) 
            }
        }
    }
}
