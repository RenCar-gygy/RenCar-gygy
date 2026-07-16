package com.turkcell.rencarapp.ui.rental.history

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.rental.RentalRepository
import com.turkcell.rencarapp.data.rental.RentalStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class RentalHistoryViewModel @Inject constructor(
    private val rentalRepository: RentalRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RentalHistoryUiState())
    val uiState: StateFlow<RentalHistoryUiState> = _uiState.asStateFlow()

    private val _effect = Channel<RentalHistoryEffect>(Channel.BUFFERED)
    val effect: Flow<RentalHistoryEffect> = _effect.receiveAsFlow()

    private val dateFormatter = DateTimeFormatter
        .ofPattern("dd MMM yyyy - HH:mm", Locale.forLanguageTag("tr-TR"))
        .withZone(ZoneId.systemDefault())

    fun onIntent(intent: RentalHistoryIntent) {
        when (intent) {
            is RentalHistoryIntent.LoadHistory -> fetchRentals()
            is RentalHistoryIntent.RentalClicked -> navigateToSummary(intent.id)
            is RentalHistoryIntent.PayRentalClicked -> navigateToSummary(intent.id)
        }
    }

    private fun navigateToSummary(rentalId: String) {
        viewModelScope.launch {
            _effect.send(RentalHistoryEffect.NavigateToSummary(rentalId))
        }
    }

    private fun fetchRentals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val rentalsDef = async { rentalRepository.listMine() }
            val statsDef = async { rentalRepository.getStats() }

            val rentalsResult = rentalsDef.await()
            val statsResult = statsDef.await()

            rentalsResult.onSuccess { rentals ->
                val completedRentals = rentals
                    .filter { it.status == RentalStatus.COMPLETED }
                    .sortedByDescending { it.startDate }

                val uiModels = completedRentals.map { rental ->
                    val vehicleName = listOf(rental.vehicleBrand, rental.vehicleModel)
                        .filter { it.isNotBlank() }
                        .joinToString(" ")
                        .ifBlank { "Araç ${rental.vehicleId.take(4).uppercase()}" }

                    RentalUiModel(
                        id = rental.id,
                        vehicleName = vehicleName,
                        dateText = dateFormatter.format(rental.startDate),
                        durationText = "${rental.durationMinutes} dk",
                        distanceText = String.format(
                            Locale.forLanguageTag("tr-TR"),
                            "%.1f km",
                            rental.distanceKm,
                        ),
                        priceText = String.format(Locale.forLanguageTag("tr-TR"), "₺%.2f", rental.totalPrice),
                        isPaid = rental.paymentStatus.equals("PAID", ignoreCase = true),
                        paymentMethodLabel = paymentMethodLabel(rental.paymentMethod),
                    )
                }

                val stats = statsResult.getOrNull()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        rentals = uiModels,
                        monthlyTripCount = stats?.tripCount ?: uiModels.size,
                        monthlyTotalSpent = stats?.totalSpent
                            ?: completedRentals
                                .filter { rental -> rental.paymentStatus.equals("PAID", ignoreCase = true) }
                                .sumOf { rental -> rental.totalPrice },
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, rentals = emptyList()) }
                _effect.send(RentalHistoryEffect.ShowError(error.message ?: "Kiralama geçmişi alınamadı."))
            }
        }
    }

    private fun paymentMethodLabel(paymentMethod: String?): String? =
        when (paymentMethod?.uppercase()) {
            "WALLET" -> "Cüzdan"
            "CARD" -> "Kart"
            else -> null
        }
}
