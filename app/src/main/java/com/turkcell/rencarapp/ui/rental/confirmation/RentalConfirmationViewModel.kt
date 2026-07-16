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
import com.turkcell.rencarapp.data.rental.dailyDurationLabel
import com.turkcell.rencarapp.data.rental.dailyQuoteMinutes
import com.turkcell.rencarapp.data.rental.defaultQuoteMinutes
import com.turkcell.rencarapp.data.rental.durationLabel
import com.turkcell.rencarapp.data.rental.requiresScheduledEndDate
import com.turkcell.rencarapp.data.reservation.ReservationRepository
import com.turkcell.rencarapp.data.vehicle.VehiclePriceFormatter
import com.turkcell.rencarapp.data.vehicle.VehicleRepository
import com.turkcell.rencarapp.ui.navigation.RenCarDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class RentalConfirmationViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val rentalRepository: RentalRepository,
    private val reservationRepository: ReservationRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val vehicleId: String = checkNotNull(savedStateHandle["vehicleId"])
    private val initialPlan: RentalPlan = savedStateHandle.get<String>(RenCarDestination.ARG_PLAN)
        ?.let { runCatching { RentalPlan.valueOf(it) }.getOrNull() }
        ?: RentalPlan.PER_MINUTE

    private val _uiState = MutableStateFlow(
        RentalConfirmationUiState(
            selectedPlan = initialPlan,
            dailyEndDate = defaultDailyEndDate(),
        )
    )
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
                _uiState.update {
                    it.copy(
                        selectedPlan = intent.plan,
                        dailyEndDate = if (intent.plan.requiresScheduledEndDate()) {
                            it.dailyEndDate ?: defaultDailyEndDate()
                        } else {
                            it.dailyEndDate
                        },
                    )
                }
                loadQuote()
            }
            is RentalConfirmationIntent.TermsAcceptedChanged -> {
                _uiState.update { it.copy(isTermsAccepted = intent.accepted) }
            }
            is RentalConfirmationIntent.CompleteReservationClicked -> createRental()
            is RentalConfirmationIntent.DailyEndDatePickerClicked -> {
                _uiState.update { it.copy(showDailyEndDatePicker = true) }
            }
            is RentalConfirmationIntent.DailyEndDatePickerDismissed -> {
                _uiState.update { it.copy(showDailyEndDatePicker = false) }
            }
            is RentalConfirmationIntent.DailyEndDateSelected -> {
                _uiState.update {
                    it.copy(
                        dailyEndDate = intent.date,
                        showDailyEndDatePicker = false,
                    )
                }
                loadQuote()
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
                            minutelyPriceLabel = VehiclePriceFormatter.planPriceLabel(vehicle, RentalPlan.PER_MINUTE),
                            hourlyPriceLabel = VehiclePriceFormatter.planPriceLabel(vehicle, RentalPlan.HOURLY),
                            dailyPriceLabel = VehiclePriceFormatter.planPriceLabel(vehicle, RentalPlan.DAILY),
                        )
                    }
                    loadQuote()
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.toUserMessage(ApiErrorContext.VEHICLE_DETAIL),
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
        val state = _uiState.value
        val plan = state.selectedPlan
        val minutes = quoteMinutes(plan, state.dailyEndDate)

        viewModelScope.launch {
            vehicleRepository.getQuote(vehicleId, plan, minutes)
                .onSuccess { quote ->
                    _uiState.update {
                        it.copy(
                            basePriceLabel = VehiclePriceFormatter.formatMoney(quote.startFee),
                            serviceFeeLabel = VehiclePriceFormatter.formatMoney(quote.serviceFee),
                            estimatedPriceLabel = VehiclePriceFormatter.formatMoney(quote.estimatedTotal),
                            estimatedDuration = durationLabelFor(plan, it.dailyEndDate),
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
                val resolved = resolveDailyEndInstant()
                if (resolved == null || !resolved.isAfter(Instant.now())) {
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(
                        RentalConfirmationEffect.ShowError("Geçerli bir iade tarihi seçin.")
                    )
                    return@launch
                }
                resolved
            } else {
                null
            }

            val request = CreateRentalRequest(
                vehicleId = vehicleId,
                plan = selectedPlan,
                endDate = endDate,
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

    private fun resolveDailyEndInstant(): Instant? {
        val date = _uiState.value.dailyEndDate ?: return null
        return dailyEndInstant(date)
    }

    private fun quoteMinutes(plan: RentalPlan, dailyEndDate: LocalDate?): Int {
        if (plan == RentalPlan.DAILY && dailyEndDate != null) {
            return dailyQuoteMinutes(dailyEndDate)
        }
        return plan.defaultQuoteMinutes()
    }

    private fun durationLabelFor(plan: RentalPlan, dailyEndDate: LocalDate?): String {
        if (plan == RentalPlan.DAILY && dailyEndDate != null) {
            return dailyDurationLabel(dailyEndDate)
        }
        return plan.durationLabel()
    }

    companion object {
        fun defaultDailyEndDate(): LocalDate = LocalDate.now()

        fun dailyEndInstant(date: LocalDate): Instant =
            date.atTime(23, 59).atZone(ZoneId.systemDefault()).toInstant()
    }
}
