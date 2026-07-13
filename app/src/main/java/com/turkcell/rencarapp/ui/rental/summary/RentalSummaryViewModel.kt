package com.turkcell.rencarapp.ui.rental.summary

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.rental.RentalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.Locale
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class RentalSummaryViewModel @Inject constructor(
    private val rentalRepository: RentalRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(RentalSummaryUiState())
    val uiState: StateFlow<RentalSummaryUiState> = _uiState.asStateFlow()

    private val _effect = Channel<RentalSummaryEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val rentalId: String? = savedStateHandle.get<String>("rentalId")

    init {
        fetchRentalSummary()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchRentalSummary() {
        if (rentalId == null) return

        viewModelScope.launch {
            val result = rentalRepository.getById(rentalId)

            result.onSuccess { rental ->
                val durationMinutes = Duration.between(rental.startDate, rental.endDate).toMinutes()
                val total = rental.totalPrice
                val serviceFee = total * 0.10
                val rentalFee = total - serviceFee

                _uiState.update { state ->
                    state.copy(
                        vehicleName = "Araç ${rental.vehicleId.take(4).uppercase()}",
                        plate = "34 RNT ${rental.vehicleId.take(2).uppercase()}",
                        durationText = "$durationMinutes dakika",
                        distanceText = "12.4 km",
                        rentalFee = String.format(Locale.forLanguageTag("tr-TR"), "₺%.2f", rentalFee),
                        serviceFee = String.format(Locale.forLanguageTag("tr-TR"), "₺%.2f", serviceFee),
                        totalFee = String.format(Locale.forLanguageTag("tr-TR"), "₺%.2f", total),
                        cardBrand = "Mastercard",
                        cardLast4 = "3241"
                    )
                }
            }.onFailure { error ->
                _effect.send(RentalSummaryEffect.ShowError(error.message ?: "Fatura bilgileri alınamadı."))
            }
        }
    }

    fun onIntent(intent: RentalSummaryIntent) {
        when (intent) {
            is RentalSummaryIntent.PayClicked -> {
                viewModelScope.launch {
                    _effect.send(RentalSummaryEffect.ShowToast("Ödeme başarıyla alındı!"))
                    _effect.send(RentalSummaryEffect.NavigateToHome)
                }
            }
            is RentalSummaryIntent.ChangeCardClicked -> {
                viewModelScope.launch {
                    _effect.send(RentalSummaryEffect.ShowToast("Kart değiştirme özelliği henüz aktif değil."))
                }
            }
        }
    }
}