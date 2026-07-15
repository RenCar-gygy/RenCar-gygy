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
            _uiState.update { it.copy(isLoading = true) }

            rentalRepository.getById(rentalId)
                .onSuccess { rental ->
                    val usageFee = (rental.totalPrice - rental.serviceFee).coerceAtLeast(0.0)
                    val vehicleName = listOf(rental.vehicleBrand, rental.vehicleModel)
                        .filter { it.isNotBlank() }
                        .joinToString(" ")
                        .ifBlank { "Araç" }

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            vehicleName = vehicleName,
                            plate = rental.vehiclePlate.ifBlank { "—" },
                            durationText = "${rental.durationMinutes} dakika",
                            distanceText = String.format(
                                Locale.forLanguageTag("tr-TR"),
                                "%.1f km",
                                rental.distanceKm
                            ),
                            rentalFee = String.format(Locale.forLanguageTag("tr-TR"), "₺%.2f", usageFee),
                            serviceFee = String.format(Locale.forLanguageTag("tr-TR"), "₺%.2f", rental.serviceFee),
                            totalFee = String.format(Locale.forLanguageTag("tr-TR"), "₺%.2f", rental.totalPrice),
                            cardBrand = "Mastercard",
                            cardLast4 = "3241"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
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
