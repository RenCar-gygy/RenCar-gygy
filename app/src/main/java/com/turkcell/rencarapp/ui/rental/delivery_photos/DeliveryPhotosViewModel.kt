package com.turkcell.rencarapp.ui.rental.delivery_photos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.rental.RentalRepository
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
class DeliveryPhotosViewModel @Inject constructor(
    private val rentalRepository: RentalRepository,
    private val vehicleRepository: VehicleRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val rentalId: String = checkNotNull(savedStateHandle["rentalId"])
    private val navVehicleName: String = savedStateHandle.get<String>("name").orEmpty()
    private val navVehiclePlate: String = savedStateHandle.get<String>("plate").orEmpty()

    private val _uiState = MutableStateFlow(
        DeliveryPhotosUiState(
            brand = navVehicleName.substringBefore(" ").trim(),
            model = navVehicleName.substringAfter(" ", missingDelimiterValue = "").trim(),
            plate = navVehiclePlate,
        )
    )
    val uiState: StateFlow<DeliveryPhotosUiState> = _uiState.asStateFlow()

    private val _effect = Channel<DeliveryPhotosEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadVehicleInfo()
    }

    fun onIntent(intent: DeliveryPhotosIntent) {
        when (intent) {
            is DeliveryPhotosIntent.PhotoCaptureRequested -> {
                viewModelScope.launch {
                    _effect.send(DeliveryPhotosEffect.LaunchCamera(intent.direction))
                }
            }
            is DeliveryPhotosIntent.PhotoCaptured -> {
                val updatedPhotos = _uiState.value.photos.toMutableMap()
                updatedPhotos[intent.direction] = intent.previewUri
                _uiState.update { it.copy(photos = updatedPhotos) }
            }
            is DeliveryPhotosIntent.CompletePhotosClicked -> navigateToSummary()
            is DeliveryPhotosIntent.BackClicked -> {
                viewModelScope.launch { _effect.send(DeliveryPhotosEffect.NavigateBack) }
            }
        }
    }

    private fun loadVehicleInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            rentalRepository.getById(rentalId)
                .onSuccess { rental ->
                    vehicleRepository.getById(rental.vehicleId)
                        .onSuccess { vehicle ->
                            _uiState.update {
                                it.copy(
                                    brand = vehicle.brand,
                                    model = vehicle.model,
                                    plate = vehicle.plate,
                                    isLoading = false,
                                )
                            }
                        }
                        .onFailure {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }

    private fun navigateToSummary() {
        if (!_uiState.value.isComplete || _uiState.value.isSubmittingPhotos) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingPhotos = true) }
            // API karşılığı yok — yalnızca yerel önizleme; özet ekranına yönlendirilir.
            _effect.send(DeliveryPhotosEffect.NavigateToSummary(rentalId))
            _uiState.update { it.copy(isSubmittingPhotos = false) }
        }
    }
}
