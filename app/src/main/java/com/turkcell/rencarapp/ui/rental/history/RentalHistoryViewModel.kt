package com.turkcell.rencarapp.ui.rental.history

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.rental.RentalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class RentalHistoryViewModel @Inject constructor(
    private val rentalRepository: RentalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RentalHistoryUiState())
    val uiState: StateFlow<RentalHistoryUiState> = _uiState.asStateFlow()

    private val _effect = Channel<RentalHistoryEffect>(Channel.BUFFERED)
    val effect: Flow<RentalHistoryEffect> = _effect.receiveAsFlow()

    // Locale kullanımı modern yönteme (forLanguageTag) çevrildi
    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm", Locale.forLanguageTag("tr-TR"))
        .withZone(ZoneId.systemDefault())

    init {
        onIntent(RentalHistoryIntent.LoadHistory)
    }

    fun onIntent(intent: RentalHistoryIntent) {
        when (intent) {
            is RentalHistoryIntent.LoadHistory -> fetchRentals()
            is RentalHistoryIntent.RentalClicked -> {
                viewModelScope.launch {
                    _effect.send(RentalHistoryEffect.ShowToast("Detay ekranı henüz aktif değil."))
                }
            }
        }
    }

    private fun fetchRentals() {
        val state = _uiState.value
        if (state.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = rentalRepository.listMine()

            result.onSuccess { rentals ->
                val uiModels = rentals.map { rental ->
                    val durationMinutes = Duration.between(rental.startDate, rental.endDate).toMinutes()

                    RentalUiModel(
                        id = rental.id,
                        vehicleName = "Araç ${rental.vehicleId}",
                        dateText = dateFormatter.format(rental.startDate),
                        durationText = "$durationMinutes dk",
                        distanceText = "12,4 km",
                        priceText = String.format(Locale.forLanguageTag("tr-TR"), "₺%.2f", rental.totalPrice)
                    )
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        rentals = uiModels,
                        monthlyTripCount = uiModels.size,
                        monthlyTotalSpent = rentals.sumOf { r -> r.totalPrice }
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false) }
                _effect.send(RentalHistoryEffect.ShowError(error.message ?: "Kiralama geçmişi alınamadı."))
            }
        }
    }
}