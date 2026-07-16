package com.turkcell.rencarapp.ui.rental.confirmation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.network.ApiErrorContext
import com.turkcell.rencarapp.data.network.toUserMessage
import com.turkcell.rencarapp.data.rental.CreateRentalRequest
import com.turkcell.rencarapp.data.rental.RentalPlan
import com.turkcell.rencarapp.data.rental.RentalRepository
import com.turkcell.rencarapp.data.rental.defaultQuoteMinutes
import com.turkcell.rencarapp.data.rental.durationLabel
import com.turkcell.rencarapp.data.rental.requiresScheduledEndDate
import com.turkcell.rencarapp.data.reservation.ReservationRepository
import com.turkcell.rencarapp.data.vehicle.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class RentalConfirmationViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val rentalRepository: RentalRepository,
    private val reservationRepository: ReservationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val vehicleId: String = checkNotNull(savedStateHandle["vehicleId"])

    private val _uiState = MutableStateFlow(RentalConfirmationUiState())
    val uiState: StateFlow<RentalConfirmationUiState> = _uiState.asStateFlow()

    private val _effect = Channel<RentalConfirmationEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        onIntent(RentalConfirmationIntent.LoadVehicle)
    }

    fun onIntent(intent: RentalConfirmationIntent) {
        when (intent) {
            is RentalConfirmationIntent.LoadVehicle -> loadVehicle()
            is RentalConfirmationIntent.BackClicked -> cancelReservationAndNavigateBack()
            is RentalConfirmationIntent.PlanSelected -> {
                _uiState.update { it.copy(selectedPlan = intent.plan) }
                loadQuote()
            }
            is RentalConfirmationIntent.TermsAcceptedChanged -> {
                _uiState.update { it.copy(isTermsAccepted = intent.accepted) }
            }
            is RentalConfirmationIntent.CompleteReservationClicked -> {
                createRental()
            }
        }
    }

    private fun cancelReservationAndNavigateBack() {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            reservationRepository.getActive()
                .onSuccess { reservation ->
                    reservationRepository.cancel(reservation.id)
                        .onFailure { error ->
                            _effect.send(
                                RentalConfirmationEffect.ShowError(
                                    error.toUserMessage(ApiErrorContext.RESERVATION_CANCEL)
                                )
                            )
                        }
                }
            _uiState.update { it.copy(isLoading = false) }
            _effect.send(RentalConfirmationEffect.NavigateBack)
        }
    }

    private fun loadVehicle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            vehicleRepository.getById(vehicleId)
                .onSuccess { vehicle ->
                    _uiState.update {
                        it.copy(
                            vehicle = vehicle,
                            isLoading = false,
                            minutelyPriceLabel = "₺${String.format("%.2f", vehicle.pricePerMinute)}/dk",
                            hourlyPriceLabel = "₺${String.format("%.2f", vehicle.pricePerHour)}/sa",
                            dailyPriceLabel = "₺${String.format("%.0f", vehicle.pricePerDay)}/gün"
                        )
                    }
                    loadQuote()
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.toUserMessage(ApiErrorContext.VEHICLE_DETAIL)
                        )
                    }
                    _effect.send(
                        RentalConfirmationEffect.ShowError(
                            exception.toUserMessage(ApiErrorContext.VEHICLE_DETAIL)
                        )
                    )
                }
        }
    }

    private fun loadQuote() {
        val plan = _uiState.value.selectedPlan

        viewModelScope.launch {
            vehicleRepository.getQuote(vehicleId, plan, plan.defaultQuoteMinutes())
                .onSuccess { quote ->
                    _uiState.update {
                        it.copy(
                            basePriceLabel = "₺${String.format("%.2f", quote.startFee)}",
                            serviceFeeLabel = "₺${String.format("%.2f", quote.serviceFee)}",
                            estimatedPriceLabel = "₺${String.format("%.2f", quote.estimatedTotal)}",
                            estimatedDuration = plan.durationLabel()
                        )
                    }
                }
        }
    }

    private fun createRental() {
        if (!_uiState.value.isTermsAccepted) {
            viewModelScope.launch {
                _effect.send(RentalConfirmationEffect.ShowError("Lütfen kullanım şartlarını onaylayın."))
            }
            return
        }

        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val selectedPlan = _uiState.value.selectedPlan
            val endDate = if (selectedPlan.requiresScheduledEndDate()) {
                calculateEndDate(selectedPlan)
            } else {
                null
            }

            val request = CreateRentalRequest(
                vehicleId = vehicleId,
                plan = selectedPlan,
                endDate = endDate
            )

            ensureActiveReservationForVehicle()
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(
                        RentalConfirmationEffect.ShowError(
                            error.toUserMessage(ApiErrorContext.RESERVATION_CREATE)
                        )
                    )
                    return@launch
                }

            rentalRepository.create(request)
                .onSuccess { rental ->
                    navigateToActiveRental(rental.id)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(
                        RentalConfirmationEffect.ShowError(
                            error.toUserMessage(ApiErrorContext.RENTAL_CREATE)
                        )
                    )
                }
        }
    }

    private suspend fun ensureActiveReservationForVehicle(): Result<Unit> {
        val active = reservationRepository.getActive().getOrNull()
        if (active != null) {
            return if (active.vehicleId == vehicleId) {
                Result.success(Unit)
            } else {
                Result.failure(
                    IllegalStateException(
                        "Aktif rezervasyonunuz başka bir araç için. Lütfen geri dönüp o aracı kullanın veya rezervasyonu iptal edin."
                    )
                )
            }
        }
        return reservationRepository.create(vehicleId).map { }
    }

    private suspend fun navigateToActiveRental(rentalId: String) {
        _uiState.update { it.copy(isLoading = false) }
        _effect.send(RentalConfirmationEffect.NavigateToActiveRental(rentalId))
    }

    private fun calculateEndDate(plan: RentalPlan): Instant {
        val now = Instant.now()
        return when (plan) {
            RentalPlan.PER_MINUTE -> now.plus(30, ChronoUnit.MINUTES)
            RentalPlan.HOURLY -> now.plus(1, ChronoUnit.HOURS)
            RentalPlan.DAILY -> now.plus(1, ChronoUnit.DAYS)
        }
    }
}
