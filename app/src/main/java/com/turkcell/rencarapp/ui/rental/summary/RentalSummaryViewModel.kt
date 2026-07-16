package com.turkcell.rencarapp.ui.rental.summary

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.network.ApiErrorContext
import com.turkcell.rencarapp.data.network.toUserMessage
import com.turkcell.rencarapp.data.rental.PayRentalRequest
import com.turkcell.rencarapp.data.rental.PaymentMethod
import com.turkcell.rencarapp.data.rental.RentalRepository
import com.turkcell.rencarapp.data.wallet.WalletRefreshNotifier
import com.turkcell.rencarapp.data.wallet.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
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
    private val walletRepository: WalletRepository,
    private val walletRefreshNotifier: WalletRefreshNotifier,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RentalSummaryUiState())
    val uiState: StateFlow<RentalSummaryUiState> = _uiState.asStateFlow()

    private val _effect = Channel<RentalSummaryEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val rentalId: String? = savedStateHandle.get<String>("rentalId")

    init {
        fetchRentalSummary()
    }

    fun onIntent(intent: RentalSummaryIntent) {
        when (intent) {
            is RentalSummaryIntent.PayClicked -> payRental()
            is RentalSummaryIntent.ChangePaymentMethodClicked ->
                _uiState.update { it.copy(showPaymentMethodSheet = true) }
            is RentalSummaryIntent.DismissPaymentMethodSheet ->
                _uiState.update { it.copy(showPaymentMethodSheet = false) }
            is RentalSummaryIntent.SelectWalletPayment -> selectWalletPayment()
            is RentalSummaryIntent.SelectCardPayment -> selectCardPayment(intent.cardId)
            is RentalSummaryIntent.DiscountCodeChanged -> {
                _uiState.update { it.copy(discountCodeInput = intent.code.uppercase()) }
            }
            is RentalSummaryIntent.NavigateHomeClicked -> {
                viewModelScope.launch { _effect.send(RentalSummaryEffect.NavigateToHome) }
            }
        }
    }

    private fun fetchRentalSummary() {
        if (rentalId == null) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val rentalDef = async { rentalRepository.getById(rentalId) }
            val cardsDef = async { walletRepository.listCards() }
            val walletDef = async { walletRepository.getWallet() }

            val rentalResult = rentalDef.await()
            val cardsResult = cardsDef.await()
            val walletResult = walletDef.await()

            rentalResult.onSuccess { rental ->
                val usageFee = (rental.totalPrice - rental.serviceFee - rental.startFee)
                    .coerceAtLeast(0.0)
                val vehicleName = listOf(rental.vehicleBrand, rental.vehicleModel)
                    .filter { it.isNotBlank() }
                    .joinToString(" ")
                    .ifBlank { "Araç" }
                val isPaid = rental.paymentStatus.equals("PAID", ignoreCase = true)
                val discountText = if (rental.discountAmount > 0) {
                    "-${formatMoney(rental.discountAmount)}"
                } else {
                    ""
                }
                val storedPaymentLabel = if (isPaid) {
                    storedPaymentMethodLabel(rental.paymentMethod, walletResult.getOrNull()?.balance)
                } else {
                    ""
                }

                val cards = cardsResult.getOrNull().orEmpty()
                val walletBalance = walletResult.getOrNull()?.balance

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        rentalId = rental.id,
                        vehicleName = vehicleName,
                        plate = rental.vehiclePlate.ifBlank { "—" },
                        durationText = "${rental.durationMinutes} dakika",
                        distanceText = String.format(
                            Locale.forLanguageTag("tr-TR"),
                            "%.1f km",
                            rental.distanceKm,
                        ),
                        rentalFee = formatMoney(usageFee),
                        startFee = if (rental.startFee > 0) formatMoney(rental.startFee) else "",
                        serviceFee = formatMoney(rental.serviceFee),
                        discount = discountText,
                        totalFee = formatMoney(rental.totalPrice),
                        payableAmount = rental.totalPrice,
                        isPaid = isPaid,
                        availableCards = cards.map {
                            PaymentCardOption(
                                id = it.id,
                                brand = it.brand,
                                last4 = it.last4,
                                isDefault = it.isDefault,
                            )
                        },
                        selectedMethod = PaymentMethod.WALLET,
                        selectedCardId = null,
                        cardBrand = "",
                        cardLast4 = "",
                        paymentMethodLabel = if (isPaid) {
                            storedPaymentLabel
                        } else {
                            paymentMethodLabel(
                                method = PaymentMethod.WALLET,
                                walletBalance = walletBalance,
                            )
                        },
                        walletBalance = walletBalance?.let { formatMoney(it) },
                        walletBalanceAmount = walletBalance,
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false) }
                _effect.send(
                    RentalSummaryEffect.ShowError(
                        error.toUserMessage(ApiErrorContext.RENTAL_PAY)
                    )
                )
            }
        }
    }

    private fun selectWalletPayment() {
        _uiState.update {
            it.copy(
                selectedMethod = PaymentMethod.WALLET,
                selectedCardId = null,
                cardBrand = "",
                cardLast4 = "",
                paymentMethodLabel = paymentMethodLabel(
                    method = PaymentMethod.WALLET,
                    walletBalance = it.walletBalanceAmount,
                ),
                showPaymentMethodSheet = false,
            )
        }
    }

    private fun selectCardPayment(cardId: String) {
        val card = _uiState.value.availableCards.firstOrNull { it.id == cardId } ?: return
        _uiState.update {
            it.copy(
                selectedMethod = PaymentMethod.CARD,
                selectedCardId = card.id,
                cardBrand = card.brand,
                cardLast4 = card.last4,
                paymentMethodLabel = paymentMethodLabel(
                    method = PaymentMethod.CARD,
                    brand = card.brand,
                    last4 = card.last4,
                ),
                showPaymentMethodSheet = false,
            )
        }
    }

    private fun payRental() {
        val state = _uiState.value
        if (state.isPaid || state.isPaying || state.isLoading || state.rentalId.isBlank()) return
        if (state.selectedMethod == PaymentMethod.CARD && state.selectedCardId == null) {
            viewModelScope.launch {
                _effect.send(RentalSummaryEffect.ShowError("Ödeme için bir kart seçin veya cüzdanı kullanın."))
            }
            return
        }
        if (state.selectedMethod == PaymentMethod.WALLET) {
            val balance = state.walletBalanceAmount
            if (balance == null) {
                viewModelScope.launch {
                    _effect.send(RentalSummaryEffect.ShowError("Cüzdan bakiyesi alınamadı. Lütfen tekrar deneyin."))
                }
                return
            }
            if (balance < state.payableAmount) {
                viewModelScope.launch {
                    _effect.send(
                        RentalSummaryEffect.ShowError(
                            "Cüzdan bakiyesi yetersiz. Mevcut: ${formatMoney(balance)}, " +
                                "ödenecek: ${formatMoney(state.payableAmount)}.",
                        ),
                    )
                }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPaying = true) }
            rentalRepository.pay(
                rentalId = state.rentalId,
                request = PayRentalRequest(
                    method = state.selectedMethod,
                    cardId = if (state.selectedMethod == PaymentMethod.CARD) state.selectedCardId else null,
                    discountCode = state.discountCodeInput.trim().ifBlank { null },
                ),
            ).onSuccess { result ->
                val paidWithWallet = result.method.equals(PaymentMethod.WALLET.name, ignoreCase = true)
                if (paidWithWallet) {
                    walletRefreshNotifier.notifyWalletChanged()
                }
                _uiState.update {
                    it.copy(
                        isPaying = false,
                        isPaid = result.paymentStatus.equals("PAID", ignoreCase = true),
                        totalFee = formatMoney(result.paidAmount),
                        discount = if (result.discountAmount > 0) {
                            "-${formatMoney(result.discountAmount)}"
                        } else {
                            ""
                        },
                        paymentMethodLabel = paymentMethodLabel(
                            method = if (paidWithWallet) PaymentMethod.WALLET else PaymentMethod.CARD,
                            brand = result.cardBrand,
                            last4 = result.cardLast4,
                            walletBalance = result.walletBalance,
                        ),
                        walletBalance = result.walletBalance?.let { bal -> formatMoney(bal) },
                        walletBalanceAmount = result.walletBalance,
                        selectedMethod = if (paidWithWallet) PaymentMethod.WALLET else PaymentMethod.CARD,
                    )
                }
                val toastMessage = if (paidWithWallet) {
                    "Ödeme cüzdanınızdan alındı."
                } else {
                    "Ödeme kartınızdan alındı."
                }
                _effect.send(RentalSummaryEffect.ShowToast(toastMessage))
                _effect.send(RentalSummaryEffect.NavigateToHome)
            }.onFailure { error ->
                _uiState.update { it.copy(isPaying = false) }
                _effect.send(RentalSummaryEffect.ShowError(error.toUserMessage(ApiErrorContext.RENTAL_PAY)))
            }
        }
    }

    private fun storedPaymentMethodLabel(
        paymentMethod: String?,
        walletBalance: Double?,
    ): String =
        when (paymentMethod?.uppercase()) {
            PaymentMethod.WALLET.name -> paymentMethodLabel(
                method = PaymentMethod.WALLET,
                walletBalance = walletBalance,
            )
            PaymentMethod.CARD.name -> paymentMethodLabel(method = PaymentMethod.CARD)
            else -> ""
        }

    private fun paymentMethodLabel(
        method: PaymentMethod,
        brand: String? = null,
        last4: String? = null,
        walletBalance: Double? = null,
    ): String =
        when (method) {
            PaymentMethod.WALLET -> {
                val balanceText = walletBalance?.let { formatMoney(it) }
                if (balanceText != null) "Cüzdan ($balanceText)" else "Cüzdan"
            }
            PaymentMethod.CARD -> {
                if (!brand.isNullOrBlank() && !last4.isNullOrBlank()) {
                    "$brand •••• $last4"
                } else {
                    "Kayıtlı kart"
                }
            }
        }

    private fun formatMoney(amount: Double): String =
        String.format(Locale.forLanguageTag("tr-TR"), "₺%.2f", amount)
}
