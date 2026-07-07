package com.turkcell.rencarapp.ui.vehicle.detail

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
class VehicleDetailViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
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
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            vehicleRepository.getById(vehicleId)
                .onSuccess { vehicle ->
                    _uiState.update { 
                        it.copy(
                            vehicle = vehicle, 
                            isLoading = false
                        ) 
                    }
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Araç bilgileri alınamadı."
                        ) 
                    }
                    _effect.send(VehicleDetailEffect.ShowMessage(exception.message ?: "Hata oluştu."))
                }
        }
    }
}
