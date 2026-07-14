package com.turkcell.rencarapp.ui.rental.active

import android.annotation.SuppressLint
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.rental.RentalRepository
import com.turkcell.rencarapp.data.reservation.ReservationRepository
import com.turkcell.rencarapp.data.vehicle.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ActiveRentalViewModel @Inject constructor(
    private val rentalRepository: RentalRepository,
    private val reservationRepository: ReservationRepository,
    private val vehicleRepository: VehicleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val rentalId: String? = savedStateHandle["rentalId"]

    private val _uiState = MutableStateFlow(ActiveRentalUiState(rentalId = rentalId.orEmpty()))
    val uiState: StateFlow<ActiveRentalUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ActiveRentalEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var pollingJob: Job? = null

    init {
        startPolling()
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }

    fun onIntent(intent: ActiveRentalIntent) {
        when (intent) {
            is ActiveRentalIntent.LoadRental -> startPolling()
            is ActiveRentalIntent.ToggleLock -> {
                _uiState.update { it.copy(isLocked = !it.isLocked) }
                viewModelScope.launch {
                    val msg = if (_uiState.value.isLocked) "Araç kilitlendi" else "Araç kilidi açıldı, iyi yolculuklar!"
                    _effect.send(ActiveRentalEffect.ShowMessage(msg))
                }
            }
            is ActiveRentalIntent.FinishRental -> finishRental()
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                // 1. Önce aktif kiralama var mı bak
                rentalRepository.getActive()
                    .onSuccess { activeRental ->
                        _uiState.update {
                            it.copy(
                                rentalId = activeRental.id,
                                vehicleName = "${activeRental.vehicle.brand} ${activeRental.vehicle.model}",
                                vehiclePlate = activeRental.vehicle.plate,
                                duration = formatSeconds(activeRental.elapsedSeconds),
                                currentPrice = String.format(Locale.getDefault(), "₺%.2f", activeRental.currentCost),
                                distance = String.format(Locale.getDefault(), "%.1f km", activeRental.distanceKm),
                                isReservationActive = false,
                                remainingReservationSeconds = null,
                                isLoading = false
                            )
                        }
                    }
                    .onFailure {
                        // 2. Kiralama yoksa aktif rezervasyon var mı bak
                        reservationRepository.getActive()
                            .onSuccess { reservation ->
                                _uiState.update {
                                    it.copy(
                                        vehicleName = "${reservation.vehicle.brand} ${reservation.vehicle.model}",
                                        vehiclePlate = reservation.vehicle.plate,
                                        duration = formatSeconds(reservation.remainingSeconds),
                                        currentPrice = "₺0,00",
                                        distance = "0,0 km",
                                        isReservationActive = true,
                                        remainingReservationSeconds = reservation.remainingSeconds,
                                        isLoading = false
                                    )
                                }
                            }
                            .onFailure { error ->
                                _uiState.update { it.copy(error = "Aktif kiralama veya rezervasyon bulunamadı.", isLoading = false) }
                            }
                    }
                delay(2000) // 2 saniyede bir güncelle
            }
        }
    }

    private fun formatSeconds(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun finishRental() {
        val currentRentalId = _uiState.value.rentalId
        if (currentRentalId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            rentalRepository.finish(currentRentalId)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(
                        ActiveRentalEffect.NavigateToDeliveryPhotos(
                            rentalId = currentRentalId,
                            vehicleName = _uiState.value.vehicleName,
                            vehiclePlate = _uiState.value.vehiclePlate
                        )
                    )
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(ActiveRentalEffect.ShowMessage(error.message ?: "Kiralama sonlandırılamadı."))
                }
        }
    }
}