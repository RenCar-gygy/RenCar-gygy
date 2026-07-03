package com.turkcell.rencarapp.ui.rental.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.vehicle.VehicleRepository
import com.turkcell.rencarapp.ui.navigation.RenCarDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RentalSummaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vehicleRepository: VehicleRepository
) : ViewModel() {

    private val vehicleId: String = checkNotNull(savedStateHandle[RenCarDestination.ARG_VEHICLE_ID])

    private val _uiState = MutableStateFlow(RentalSummaryUiState(vehicleId = vehicleId))
    val uiState: StateFlow<RentalSummaryUiState> = _uiState.asStateFlow()

    private val _effect = Channel<RentalSummaryEffect>(Channel.BUFFERED)
    val effect: Flow<RentalSummaryEffect> = _effect.receiveAsFlow()

    init {
        loadVehicleDetails()
    }

    fun onIntent(intent: RentalSummaryIntent) {
        when (intent) {
            is RentalSummaryIntent.PayClicked -> {
                viewModelScope.launch {
                    // Dokümanlara göre akış: Özet -> Teslim Fotoğrafı
                    _effect.send(RentalSummaryEffect.NavigateToDeliveryPhotos(vehicleId))
                }
            }
            is RentalSummaryIntent.ChangeCardClicked -> {
                viewModelScope.launch {
                    _effect.send(RentalSummaryEffect.ShowToast("Kart değiştirme henüz aktif değil (Stub)"))
                }
            }
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