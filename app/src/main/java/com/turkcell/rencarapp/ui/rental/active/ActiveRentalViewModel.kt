package com.turkcell.rencarapp.ui.rental.active

import android.annotation.SuppressLint
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.rental.Rental
import com.turkcell.rencarapp.data.rental.RentalRepository
import com.turkcell.rencarapp.data.vehicle.Vehicle
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
import java.time.Instant
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ActiveRentalViewModel @Inject constructor(
    private val rentalRepository: RentalRepository,
    private val vehicleRepository: VehicleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val rentalId: String = checkNotNull(savedStateHandle["rentalId"])

    private val _uiState = MutableStateFlow(ActiveRentalUiState(rentalId = rentalId))
    val uiState: StateFlow<ActiveRentalUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ActiveRentalEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var timerJob: Job? = null

    init {
        onIntent(ActiveRentalIntent.LoadRental)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    fun onIntent(intent: ActiveRentalIntent) {
        when (intent) {
            is ActiveRentalIntent.LoadRental -> loadRental()
            is ActiveRentalIntent.ToggleLock -> {
                _uiState.update { it.copy(isLocked = !it.isLocked) }
                viewModelScope.launch {
                    val msg = if (_uiState.value.isLocked) "Araç kilitlendi" else "Araç kilidi açıldı"
                    _effect.send(ActiveRentalEffect.ShowMessage(msg))
                }
            }
            is ActiveRentalIntent.FinishRental -> finishRental()
        }
    }

    private fun loadRental() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            rentalRepository.getById(rentalId)
                .onSuccess { rental ->
                    _uiState.update { it.copy(rentalId = rental.id) }
                    loadVehicle(rental)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                    _effect.send(ActiveRentalEffect.ShowMessage(error.message ?: "Kiralama yüklenemedi."))
                }
        }
    }

    private fun loadVehicle(rental: Rental) {
        viewModelScope.launch {
            vehicleRepository.getById(rental.vehicleId)
                .onSuccess { vehicle ->
                    _uiState.update {
                        it.copy(
                            vehicleName = "${vehicle.brand} ${vehicle.model}",
                            isLoading = false
                        )
                    }
                    startTimer(rental, vehicle)
                }
                .onFailure {
                    _uiState.update { it.copy(isLoading = false) }
                    startTimer(rental, null)
                }
        }
    }

    @SuppressLint("NewApi")
    private fun startTimer(rental: Rental, vehicle: Vehicle?) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val startSeconds = rental.startDate.epochSecond
            while (true) {
                val nowSeconds = System.currentTimeMillis() / 1000
                val elapsedSeconds = nowSeconds - startSeconds
                
                val seconds = elapsedSeconds % 60
                val minutes = (elapsedSeconds / 60) % 60
                val hours = (elapsedSeconds / 3600)
                
                val durationString = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

                val currentPrice = if (vehicle != null) {
                    val dailyPrice = vehicle.pricePerDay
                    val hourlyPrice = dailyPrice / 8
                    val minutelyRate = hourlyPrice / 40
                    val elapsedMinutes = elapsedSeconds / 60.0
                    val basePrice = 15.0 // Varsayılan başlangıç ücreti
                    val totalPrice = basePrice + (minutelyRate * elapsedMinutes)
                    String.format(Locale.getDefault(), "₺%.2f", totalPrice)
                } else {
                    _uiState.value.currentPrice
                }

                _uiState.update { 
                    it.copy(
                        duration = durationString,
                        currentPrice = currentPrice
                    )
                }
                delay(1000)
            }
        }
    }

    private fun finishRental() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            rentalRepository.returnRental(rentalId)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(ActiveRentalEffect.NavigateToMain)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(ActiveRentalEffect.ShowMessage(error.message ?: "Kiralama sonlandırılamadı."))
                }
        }
    }
}
