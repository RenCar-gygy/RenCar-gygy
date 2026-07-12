package com.turkcell.rencarapp.ui.rental.delivery_photos

import android.net.Uri
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
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val rentalId: String = checkNotNull(savedStateHandle["rentalId"])
    private val navVehicleName: String = savedStateHandle.get<String>("name").orEmpty()
    private val navVehiclePlate: String = savedStateHandle.get<String>("plate").orEmpty()

    private val _uiState = MutableStateFlow(
        DeliveryPhotosUiState(
            brand = navVehicleName.substringBefore(" ").trim(),
            model = navVehicleName.substringAfter(" ", missingDelimiterValue = "").trim(),
            plate = navVehiclePlate
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
            is DeliveryPhotosIntent.PhotoCaptured -> {
                _uiState.update { currentState ->
                    val updatedPhotos = currentState.photos.toMutableMap()
                    updatedPhotos[intent.direction] = intent.uri
                    currentState.copy(photos = updatedPhotos)
                }
            }
            is DeliveryPhotosIntent.StartRentalClicked -> startRental()
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
                                    isLoading = false
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

    private fun startRental() {
        if (!_uiState.value.isComplete) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isStartingRental = true) }
            // API'da fotoğraf yükleme olmadığı için sadece simüle ediyoruz
            kotlinx.coroutines.delay(800)
            _uiState.update { it.copy(isStartingRental = false) }
            _effect.send(DeliveryPhotosEffect.NavigateToSummary(rentalId))
        }
    }
}
