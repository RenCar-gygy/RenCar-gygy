package com.turkcell.rencarapp.ui.rental.active

import android.annotation.SuppressLint
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.rental.Rental
import com.turkcell.rencarapp.data.rental.RentalRepository
import com.turkcell.rencarapp.data.vehicle.Vehicle
import com.turkcell.rencarapp.data.vehicle.VehiclePriceFormatter
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
    private var usageStartTime: Long? = null

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
                val currentlyLocked = _uiState.value.isLocked
                if (currentlyLocked && usageStartTime == null) {
                    // İlk kez kilit açılıyor -> Kullanım başlar
                    usageStartTime = System.currentTimeMillis() / 1000
                }
                
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
            val reservationStartSeconds = rental.startDate.epochSecond
            val reservationLimitSeconds = reservationStartSeconds + (15 * 60) // 15 dk ücretsiz

            while (true) {
                val nowSeconds = System.currentTimeMillis() / 1000
                val startOfUsage = usageStartTime
                
                val durationString: String
                val currentPrice: String

                if (startOfUsage == null) {
                    // REZERVASYON MODU (Geri Sayım)
                    val remainingSeconds = reservationLimitSeconds - nowSeconds
                    val absSeconds = kotlin.math.abs(remainingSeconds)
                    
                    val seconds = absSeconds % 60
                    val minutes = (absSeconds / 60) % 60
                    val hours = absSeconds / 3600
                    
                    val sign = if (remainingSeconds < 0) "-" else ""
                    // Rezervasyon süresini 00:15:00 formatında gösteriyoruz
                    durationString = String.format(Locale.getDefault(), "%s%02d:%02d:%02d", sign, hours, minutes, seconds)
                    currentPrice = "₺0,00"
                } else {
                    // KULLANIM MODU (İleri Sayım)
                    val elapsedSeconds = nowSeconds - startOfUsage
                    val seconds = elapsedSeconds % 60
                    val minutes = (elapsedSeconds / 60) % 60
                    val hours = elapsedSeconds / 3600
                    
                    durationString = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

                    val basePrice = 15.0 // Açılış ücreti
                    currentPrice = if (vehicle != null) {
                        val minutelyRate = VehiclePriceFormatter.minutelyPrice(vehicle.pricePerDay)
                        val elapsedMinutes = elapsedSeconds / 60.0
                        val totalPrice = basePrice + (minutelyRate * elapsedMinutes)
                        String.format(Locale.getDefault(), "₺%.2f", totalPrice)
                    } else {
                        // Araç bilgisi henüz gelmediyse bile başlangıç ücretini göster
                        String.format(Locale.getDefault(), "₺%.2f", basePrice)
                    }
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
                    _effect.send(ActiveRentalEffect.NavigateToSummary(rentalId))
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(ActiveRentalEffect.ShowMessage(error.message ?: "Kiralama sonlandırılamadı."))
                }
        }
    }
}
