package com.turkcell.rencarapp.ui.vehicle.detail

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
class VehicleDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Route'dan gelen vehicleId parametresi
    private val vehicleId: String = checkNotNull(savedStateHandle["vehicleId"])

    private val _uiState = MutableStateFlow(VehicleDetailUiState())
    val uiState: StateFlow<VehicleDetailUiState> = _uiState.asStateFlow()

    private val _effect = Channel<VehicleDetailEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        onIntent(VehicleDetailIntent.LoadVehicle)
    }

    /**
     * UI'dan gelen niyetleri işler.
     */
    fun onIntent(intent: VehicleDetailIntent) {
        when (intent) {
            is VehicleDetailIntent.LoadVehicle -> loadVehicle()
            is VehicleDetailIntent.BackClicked -> {
                viewModelScope.launch { _effect.send(VehicleDetailEffect.NavigateBack) }
            }
            is VehicleDetailIntent.ReserveClicked -> {
                viewModelScope.launch {
                    _effect.send(VehicleDetailEffect.NavigateToConfirmation(vehicleId))
                }
            }
            is VehicleDetailIntent.UnlockClicked -> {
                viewModelScope.launch {
                    _effect.send(VehicleDetailEffect.ShowMessage("Kilit açma özelliği şu an aktif değil."))
                }
            }
        }
    }

    private fun loadVehicle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Simüle edilmiş gecikme ve sahte veri (Fake Repository mantığı)
            // Normalde VehicleRepository üzerinden çekilmeli
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
