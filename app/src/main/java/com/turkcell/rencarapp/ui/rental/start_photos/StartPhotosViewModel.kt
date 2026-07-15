package com.turkcell.rencarapp.ui.rental.start_photos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.network.ApiErrorContext
import com.turkcell.rencarapp.data.network.toUserMessage
import com.turkcell.rencarapp.data.rental.RentalPhotoStub
import com.turkcell.rencarapp.data.rental.RentalRepository
import com.turkcell.rencarapp.data.vehicle.VehicleRepository
import com.turkcell.rencarapp.ui.rental.delivery_photos.MARKED_PHOTO_URI
import com.turkcell.rencarapp.ui.rental.delivery_photos.PhotoDirection
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
class StartPhotosViewModel @Inject constructor(
    private val rentalRepository: RentalRepository,
    private val vehicleRepository: VehicleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val rentalId: String = checkNotNull(savedStateHandle["rentalId"])
    private val navVehicleName: String = savedStateHandle.get<String>("name").orEmpty()
    private val navVehiclePlate: String = savedStateHandle.get<String>("plate").orEmpty()

    private val _uiState = MutableStateFlow(
        StartPhotosUiState(
            brand = navVehicleName.substringBefore(" ").trim(),
            model = navVehicleName.substringAfter(" ", missingDelimiterValue = "").trim(),
            plate = navVehiclePlate
        )
    )
    val uiState: StateFlow<StartPhotosUiState> = _uiState.asStateFlow()

    private val _effect = Channel<StartPhotosEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadInitialState()
    }

    fun onIntent(intent: StartPhotosIntent) {
        when (intent) {
            is StartPhotosIntent.PhotoBoxToggled -> togglePhotoBox(intent.direction)
            is StartPhotosIntent.CompletePhotosClicked -> submitPhotosAndStart()
            is StartPhotosIntent.BackClicked -> {
                viewModelScope.launch { _effect.send(StartPhotosEffect.NavigateBack) }
            }
        }
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            rentalRepository.getById(rentalId)
                .onSuccess { rental ->
                    vehicleRepository.getById(rental.vehicleId)
                        .onSuccess { vehicle ->
                            _uiState.update {
                                it.copy(
                                    brand = vehicle.brand,
                                    model = vehicle.model,
                                    plate = vehicle.plate,
                                )
                            }
                        }
                }

            rentalRepository.getPhotos(rentalId)
                .onSuccess { photoState ->
                    val uploadedSides = RentalPhotoStub.REQUIRED_SIDES
                        .filter { it !in photoState.remainingSides }
                        .toSet()
                    val photos = PhotoDirection.entries.associateWith { direction ->
                        if (direction.name in uploadedSides) MARKED_PHOTO_URI else null
                    }
                    _uiState.update {
                        it.copy(
                            photos = photos,
                            isLoading = false,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.toUserMessage(ApiErrorContext.RENTAL_PHOTO)
                        )
                    }
                }
        }
    }

    private fun togglePhotoBox(direction: PhotoDirection) {
        val updatedPhotos = _uiState.value.photos.toMutableMap()
        updatedPhotos[direction] = if (updatedPhotos[direction] == null) {
            MARKED_PHOTO_URI
        } else {
            null
        }
        _uiState.update { it.copy(photos = updatedPhotos) }
    }

    private fun submitPhotosAndStart() {
        if (!_uiState.value.isComplete || _uiState.value.isSubmittingPhotos) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingPhotos = true, error = null) }

            for (direction in PhotoDirection.entries) {
                val uploadResult = rentalRepository.uploadPhotoBytes(
                    rentalId = rentalId,
                    side = direction.name,
                    bytes = RentalPhotoStub.JPEG_BYTES
                )
                if (uploadResult.isFailure) {
                    _uiState.update { it.copy(isSubmittingPhotos = false) }
                    _effect.send(
                        StartPhotosEffect.ShowError(
                            uploadResult.exceptionOrNull()
                                ?.toUserMessage(ApiErrorContext.RENTAL_PHOTO)
                                ?: "Fotoğraf yüklenemedi."
                        )
                    )
                    return@launch
                }
            }

            rentalRepository.start(rentalId)
                .onSuccess {
                    _uiState.update { it.copy(isSubmittingPhotos = false) }
                    _effect.send(StartPhotosEffect.RideStarted)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSubmittingPhotos = false) }
                    _effect.send(
                        StartPhotosEffect.ShowError(
                            error.toUserMessage(ApiErrorContext.RENTAL_START)
                        )
                    )
                }
        }
    }
}
