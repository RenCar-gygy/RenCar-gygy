package com.turkcell.rencarapp.ui.rental.summary

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.network.ApiErrorContext
import com.turkcell.rencarapp.data.network.dto.InitializeCheckoutFormDto
import com.turkcell.rencarapp.data.network.dto.IyzicoPaymentResponseDto
import com.turkcell.rencarapp.data.network.toUserMessage
import com.turkcell.rencarapp.data.payment.IyzicoRepository
import com.turkcell.rencarapp.ui.common.IyzicoCheckoutSupport
import com.turkcell.rencarapp.data.rental.PayRentalRequest
import com.turkcell.rencarapp.data.rental.PaymentMethod
import com.turkcell.rencarapp.data.rental.RentalRepository
import com.turkcell.rencarapp.data.wallet.SavedCard
import com.turkcell.rencarapp.data.wallet.WalletRefreshNotifier
import com.turkcell.rencarapp.data.wallet.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
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

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class RentalSummaryViewModel @Inject constructor(
    private val rentalRepository: RentalRepository,
    private val walletRepository: WalletRepository,
    private val iyzicoRepository: IyzicoRepository,
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
            is RentalSummaryIntent.SelectIyzicoPayment -> selectIyzicoPayment()
            is RentalSummaryIntent.SelectCardPayment -> selectCardPayment(intent.cardId)
            is RentalSummaryIntent.AddCardClicked ->
                _uiState.update { it.copy(showAddCardSheet = true) }
            is RentalSummaryIntent.DismissAddCardSheet ->
                _uiState.update { it.copy(showAddCardSheet = false) }
            is RentalSummaryIntent.SubmitNewCard -> submitNewCard(intent.number, intent.expiry)
            is RentalSummaryIntent.DiscountCodeChanged -> {
                _uiState.update { it.copy(discountCodeInput = intent.code.uppercase()) }
            }
            is RentalSummaryIntent.NavigateHomeClicked -> {
                viewModelScope.launch { _effect.send(RentalSummaryEffect.NavigateToHome) }
            }
            is RentalSummaryIntent.DismissIyzicoCheckout -> dismissIyzicoCheckout()
            is RentalSummaryIntent.IyzicoCheckoutCompleted -> completeIyzicoCheckout()
            is RentalSummaryIntent.IyzicoCheckoutSessionExpired -> handleIyzicoSessionExpired()
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
                        availableCards = cards.map(::toPaymentCardOption),
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

    private fun selectIyzicoPayment() {
        _uiState.update {
            it.copy(
                selectedMethod = PaymentMethod.IYZICO,
                selectedCardId = null,
                cardBrand = "",
                cardLast4 = "",
                paymentMethodLabel = paymentMethodLabel(method = PaymentMethod.IYZICO),
                showPaymentMethodSheet = false,
                discountCodeInput = "",
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

    private fun submitNewCard(number: String, expiry: String) {
        val digits = number.filter { it.isDigit() }
        if (digits.length < 15) {
            viewModelScope.launch {
                _effect.send(RentalSummaryEffect.ShowError("Geçerli bir kart numarası girin."))
            }
            return
        }
        val (expMonth, expYear) = parseExpiry(expiry)
            ?: run {
                viewModelScope.launch {
                    _effect.send(
                        RentalSummaryEffect.ShowError("Son kullanma tarihi AA/YY formatında olmalıdır."),
                    )
                }
                return
            }

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingCard = true) }
            walletRepository.addCard(
                brand = detectBrand(digits),
                last4 = digits.takeLast(4),
                expMonth = expMonth,
                expYear = expYear,
            ).onSuccess { savedCard ->
                walletRefreshNotifier.notifyWalletChanged()
                refreshCardsAfterAdd(savedCard)
            }.onFailure { error ->
                _uiState.update { it.copy(isAddingCard = false) }
                _effect.send(RentalSummaryEffect.ShowError(error.toUserMessage(ApiErrorContext.WALLET)))
            }
        }
    }

    private suspend fun refreshCardsAfterAdd(savedCard: SavedCard) {
        walletRepository.listCards()
            .onSuccess { cards ->
                _uiState.update {
                    it.copy(
                        isAddingCard = false,
                        showAddCardSheet = false,
                        showPaymentMethodSheet = false,
                        availableCards = cards.map(::toPaymentCardOption),
                        selectedMethod = PaymentMethod.CARD,
                        selectedCardId = savedCard.id,
                        cardBrand = savedCard.brand,
                        cardLast4 = savedCard.last4,
                        paymentMethodLabel = paymentMethodLabel(
                            method = PaymentMethod.CARD,
                            brand = savedCard.brand,
                            last4 = savedCard.last4,
                        ),
                    )
                }
                _effect.send(RentalSummaryEffect.ShowToast("Kart başarıyla eklendi."))
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        isAddingCard = false,
                        showAddCardSheet = false,
                        showPaymentMethodSheet = false,
                        availableCards = it.availableCards + toPaymentCardOption(savedCard),
                        selectedMethod = PaymentMethod.CARD,
                        selectedCardId = savedCard.id,
                        cardBrand = savedCard.brand,
                        cardLast4 = savedCard.last4,
                        paymentMethodLabel = paymentMethodLabel(
                            method = PaymentMethod.CARD,
                            brand = savedCard.brand,
                            last4 = savedCard.last4,
                        ),
                    )
                }
                _effect.send(RentalSummaryEffect.ShowToast("Kart eklendi ve seçildi."))
            }
    }

    private fun toPaymentCardOption(card: SavedCard): PaymentCardOption =
        PaymentCardOption(
            id = card.id,
            brand = card.brand,
            last4 = card.last4,
            isDefault = card.isDefault,
        )

    private fun detectBrand(number: String): String =
        when (number.firstOrNull()) {
            '4' -> "VISA"
            '5' -> "MASTERCARD"
            else -> "VISA"
        }

    private fun parseExpiry(expiry: String): Pair<Int, Int>? {
        val parts = expiry.split("/").map { it.trim() }
        if (parts.size != 2) return null
        val month = parts[0].toIntOrNull() ?: return null
        val yearPart = parts[1].toIntOrNull() ?: return null
        if (month !in 1..12) return null
        val year = if (yearPart < 100) 2000 + yearPart else yearPart
        return month to year
    }

    private fun payRental() {
        val state = _uiState.value
        if (state.isPaid || state.isPaying || state.isLoading || state.rentalId.isBlank()) return

        when (state.selectedMethod) {
            PaymentMethod.IYZICO -> launchIyzicoCheckout()
            PaymentMethod.CARD -> payWithWalletOrCard(state)
            PaymentMethod.WALLET -> payWithWalletOrCard(state)
        }
    }

    private fun payWithWalletOrCard(state: RentalSummaryUiState) {
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
            ).onSuccess { result -> handlePaySuccess(result.method, result) }
                .onFailure { error ->
                    _uiState.update { it.copy(isPaying = false) }
                    _effect.send(RentalSummaryEffect.ShowError(error.toUserMessage(ApiErrorContext.RENTAL_PAY)))
                }
        }
    }

    private fun launchIyzicoCheckout() {
        val state = _uiState.value
        if (state.payableAmount < 1.0) {
            viewModelScope.launch {
                _effect.send(RentalSummaryEffect.ShowError("Geçersiz ödeme tutarı."))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPaying = true) }
            iyzicoRepository.initializeCheckoutForm(
                InitializeCheckoutFormDto(
                    price = state.payableAmount,
                    description = "RenCar yolculuk ödemesi",
                    basketId = "rental-${state.rentalId}",
                    enabledInstallments = IyzicoCheckoutSupport.DEFAULT_ENABLED_INSTALLMENTS,
                ),
            ).onSuccess { response ->
                if (!IyzicoCheckoutSupport.isInitializeSuccess(response.status)) {
                    _uiState.update { it.copy(isPaying = false) }
                    _effect.send(RentalSummaryEffect.ShowError("İyzico oturumu başlatılamadı."))
                    return@onSuccess
                }
                val pageUrl = response.paymentPageUrl?.takeIf { it.isNotBlank() }
                val formContent = response.checkoutFormContent?.takeIf { it.isNotBlank() }
                if (pageUrl == null && formContent == null) {
                    _uiState.update { it.copy(isPaying = false) }
                    _effect.send(RentalSummaryEffect.ShowError("İyzico ödeme sayfası alınamadı."))
                    return@onSuccess
                }
                _uiState.update {
                    it.copy(
                        isPaying = false,
                        showIyzicoCheckout = true,
                        iyzicoPaymentPageUrl = pageUrl,
                        iyzicoCheckoutFormContent = formContent,
                        iyzicoCheckoutToken = response.token,
                        iyzicoCheckoutExpiresAtEpochMs = IyzicoCheckoutSupport.resolveExpiresAtEpochMs(
                            response.tokenExpireTime,
                        ),
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isPaying = false) }
                _effect.send(
                    RentalSummaryEffect.ShowError(
                        error.message ?: "İyzico oturumu başlatılamadı.",
                    ),
                )
            }
        }
    }

    private fun dismissIyzicoCheckout() {
        _uiState.update {
            it.copy(
                showIyzicoCheckout = false,
                iyzicoPaymentPageUrl = null,
                iyzicoCheckoutFormContent = null,
                iyzicoCheckoutToken = null,
                iyzicoCheckoutExpiresAtEpochMs = null,
                isPaying = false,
            )
        }
    }

    private fun handleIyzicoSessionExpired() {
        dismissIyzicoCheckout()
        viewModelScope.launch {
            _effect.send(
                RentalSummaryEffect.ShowError("İyzico oturumu süresi doldu. Lütfen tekrar deneyin."),
            )
        }
    }

    private fun completeIyzicoCheckout() {
        val state = _uiState.value
        val token = state.iyzicoCheckoutToken
        if (token.isNullOrBlank()) {
            viewModelScope.launch {
                _effect.send(RentalSummaryEffect.ShowError("İyzico oturum bilgisi bulunamadı."))
            }
            dismissIyzicoCheckout()
            return
        }
        if (IyzicoCheckoutSupport.isSessionExpired(state.iyzicoCheckoutExpiresAtEpochMs)) {
            handleIyzicoSessionExpired()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPaying = true) }
            pollIyzicoCheckoutResult(token)
                .onSuccess { result ->
                    val paymentId = result.paymentId
                    if (paymentId.isNullOrBlank()) {
                        _uiState.update { it.copy(isPaying = false) }
                        dismissIyzicoCheckout()
                        _effect.send(RentalSummaryEffect.ShowError("İyzico ödemesi tamamlanamadı."))
                        return@onSuccess
                    }
                    rentalRepository.pay(
                        rentalId = state.rentalId,
                        request = PayRentalRequest(
                            method = PaymentMethod.IYZICO,
                            iyzicoPaymentId = paymentId,
                        ),
                    ).onSuccess { payResult ->
                        dismissIyzicoCheckout()
                        handlePaySuccess(payResult.method, payResult)
                    }.onFailure { error ->
                        _uiState.update { it.copy(isPaying = false) }
                        dismissIyzicoCheckout()
                        _effect.send(RentalSummaryEffect.ShowError(error.toUserMessage(ApiErrorContext.RENTAL_PAY)))
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isPaying = false) }
                    dismissIyzicoCheckout()
                    _effect.send(
                        RentalSummaryEffect.ShowError(
                            error.message ?: "İyzico ödeme sonucu alınamadı.",
                        ),
                    )
                }
        }
    }

    private suspend fun pollIyzicoCheckoutResult(token: String): Result<IyzicoPaymentResponseDto> {
        var lastError: Throwable? = null
        repeat(IyzicoCheckoutSupport.RESULT_POLL_ATTEMPTS) { attempt ->
            val result = iyzicoRepository.getCheckoutFormResult(token)
            result.onSuccess { response ->
                when {
                    IyzicoCheckoutSupport.isCheckoutPaymentSuccess(response.paymentStatus) -> return result
                    IyzicoCheckoutSupport.isCheckoutPaymentFailure(response.paymentStatus) ->
                        return Result.failure(IllegalStateException("İyzico ödemesi başarısız oldu."))
                }
            }.onFailure { error -> lastError = error }
            if (attempt < IyzicoCheckoutSupport.RESULT_POLL_ATTEMPTS - 1) {
                delay(IyzicoCheckoutSupport.RESULT_POLL_DELAY_MS)
            }
        }
        return Result.failure(
            lastError ?: IllegalStateException("İyzico ödemesi henüz tamamlanmadı. Lütfen tekrar deneyin."),
        )
    }

    private suspend fun handlePaySuccess(method: String, result: com.turkcell.rencarapp.data.rental.PayRentalResult) {
        val paidWithWallet = method.equals(PaymentMethod.WALLET.name, ignoreCase = true)
        val paidWithIyzico = method.equals(PaymentMethod.IYZICO.name, ignoreCase = true)
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
                paymentMethodLabel = when {
                    paidWithWallet -> paymentMethodLabel(
                        method = PaymentMethod.WALLET,
                        walletBalance = result.walletBalance,
                    )
                    paidWithIyzico -> paymentMethodLabel(method = PaymentMethod.IYZICO)
                    else -> paymentMethodLabel(
                        method = PaymentMethod.CARD,
                        brand = result.cardBrand,
                        last4 = result.cardLast4,
                    )
                },
                walletBalance = result.walletBalance?.let { bal -> formatMoney(bal) },
                walletBalanceAmount = result.walletBalance,
                selectedMethod = when {
                    paidWithWallet -> PaymentMethod.WALLET
                    paidWithIyzico -> PaymentMethod.IYZICO
                    else -> PaymentMethod.CARD
                },
            )
        }
        val toastMessage = when {
            paidWithWallet -> "Ödeme cüzdanınızdan alındı."
            paidWithIyzico -> "İyzico ile ödeme alındı."
            else -> "Ödeme kartınızdan alındı."
        }
        _effect.send(RentalSummaryEffect.ShowToast(toastMessage))
        _effect.send(RentalSummaryEffect.NavigateToHome)
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
            PaymentMethod.IYZICO.name -> paymentMethodLabel(method = PaymentMethod.IYZICO)
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
            PaymentMethod.IYZICO -> "İyzico"
        }

    private fun formatMoney(amount: Double): String =
        String.format(Locale.forLanguageTag("tr-TR"), "₺%.2f", amount)
}
