package com.turkcell.rencarapp.ui.payment.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.network.ApiErrorContext
import com.turkcell.rencarapp.data.network.toUserMessage
import com.turkcell.rencarapp.data.wallet.SavedCard
import com.turkcell.rencarapp.data.wallet.WalletRefreshNotifier
import com.turkcell.rencarapp.data.wallet.WalletRepository
import com.turkcell.rencarapp.data.wallet.WalletTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val walletRefreshNotifier: WalletRefreshNotifier,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<WalletEffect>()
    val effect = _effect.asSharedFlow()

    private val dateFormatter = DateTimeFormatter
        .ofPattern("dd MMM yyyy - HH:mm", Locale.forLanguageTag("tr-TR"))
        .withZone(ZoneId.systemDefault())

    init {
        viewModelScope.launch {
            walletRefreshNotifier.version.collect { version ->
                if (version > 0) {
                    fetchAllData()
                }
            }
        }
    }

    fun onIntent(intent: WalletIntent) {
        when (intent) {
            is WalletIntent.FetchInitialData -> fetchAllData()
            is WalletIntent.AddBalanceClicked -> _uiState.update { it.copy(showDepositDialog = true) }
            is WalletIntent.DismissDepositDialog -> _uiState.update { it.copy(showDepositDialog = false) }
            is WalletIntent.Deposit -> depositMoney(intent.amount)
            is WalletIntent.AddCardClicked -> _uiState.update { it.copy(showCardBottomSheet = true) }
            is WalletIntent.DismissCardBottomSheet -> _uiState.update { it.copy(showCardBottomSheet = false) }
            is WalletIntent.SubmitNewCard -> addNewCard(intent.number, intent.expiry)
            is WalletIntent.DeleteCardClicked -> deleteCard(intent.cardId)
            is WalletIntent.SetDefaultCardClicked -> setDefaultCard(intent.cardId)
        }
    }

    private fun fetchAllData() {
        _uiState.update {
            it.copy(
                isLoading = true,
                error = null,
                balance = "0,00 ₺",
                savedCards = emptyList(),
                recentTransactions = emptyList(),
            )
        }
        viewModelScope.launch {
            val walletDef = async { walletRepository.getWallet() }
            val cardsDef = async { walletRepository.listCards() }

            val walletResult = walletDef.await()
            val cardsResult = cardsDef.await()

            if (walletResult.isFailure && cardsResult.isFailure) {
                val message = walletResult.exceptionOrNull()
                    ?.toUserMessage(ApiErrorContext.WALLET)
                    ?: "Cüzdan bilgileri alınamadı."
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = message,
                        savedCards = emptyList(),
                        recentTransactions = emptyList(),
                        balance = "0,00 ₺",
                    )
                }
                _effect.emit(WalletEffect.ShowError(message))
                return@launch
            }

            walletResult.onSuccess { wallet ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        balance = formatBalance(wallet.balance),
                        recentTransactions = wallet.transactions.map { it.toUiModel() },
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        balance = "0,00 ₺",
                        recentTransactions = emptyList(),
                    )
                }
                _effect.emit(WalletEffect.ShowError(error.toUserMessage(ApiErrorContext.WALLET)))
            }

            cardsResult.onSuccess { cards ->
                _uiState.update { it.copy(savedCards = cards.map { card -> card.toUiModel() }) }
            }.onFailure {
                _uiState.update { it.copy(savedCards = emptyList()) }
            }
        }
    }

    private fun depositMoney(amount: Double) {
        _uiState.update { it.copy(showDepositDialog = false, isActionLoading = true) }
        viewModelScope.launch {
            walletRepository.topup(amount)
                .onSuccess { wallet ->
                    _uiState.update {
                        it.copy(
                            balance = formatBalance(wallet.balance),
                            isActionLoading = false,
                            recentTransactions = wallet.transactions.map { tx -> tx.toUiModel() },
                        )
                    }
                    _effect.emit(WalletEffect.ShowToast("Bakiye başarıyla yüklendi."))
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isActionLoading = false) }
                    _effect.emit(WalletEffect.ShowError(error.toUserMessage(ApiErrorContext.WALLET)))
                }
        }
    }

    private fun addNewCard(number: String, expiry: String) {
        val digits = number.filter { it.isDigit() }
        if (digits.length < 15) {
            viewModelScope.launch {
                _effect.emit(WalletEffect.ShowError("Geçerli bir kart numarası girin."))
            }
            return
        }
        val (expMonth, expYear) = parseExpiry(expiry)
            ?: run {
                viewModelScope.launch {
                    _effect.emit(WalletEffect.ShowError("Son kullanma tarihi AA/YY formatında olmalıdır."))
                }
                return
            }

        _uiState.update { it.copy(showCardBottomSheet = false, isActionLoading = true) }
        viewModelScope.launch {
            walletRepository.addCard(
                brand = detectBrand(digits),
                last4 = digits.takeLast(4),
                expMonth = expMonth,
                expYear = expYear,
            ).onSuccess {
                fetchAllData()
                _uiState.update { state -> state.copy(isActionLoading = false) }
                _effect.emit(WalletEffect.ShowToast("Kart başarıyla eklendi."))
            }.onFailure { error ->
                _uiState.update { it.copy(isActionLoading = false) }
                _effect.emit(WalletEffect.ShowError(error.toUserMessage(ApiErrorContext.WALLET)))
            }
        }
    }

    private fun deleteCard(cardId: String) {
        _uiState.update { it.copy(isActionLoading = true) }
        viewModelScope.launch {
            walletRepository.deleteCard(cardId)
                .onSuccess {
                    fetchAllData()
                    _uiState.update { it.copy(isActionLoading = false) }
                    _effect.emit(WalletEffect.ShowToast("Kart silindi."))
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isActionLoading = false) }
                    _effect.emit(WalletEffect.ShowError(error.toUserMessage(ApiErrorContext.WALLET)))
                }
        }
    }

    private fun setDefaultCard(cardId: String) {
        _uiState.update { it.copy(isActionLoading = true) }
        viewModelScope.launch {
            walletRepository.setDefaultCard(cardId)
                .onSuccess {
                    fetchAllData()
                    _uiState.update { it.copy(isActionLoading = false) }
                    _effect.emit(WalletEffect.ShowToast("Varsayılan kart güncellendi."))
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isActionLoading = false) }
                    _effect.emit(WalletEffect.ShowError(error.toUserMessage(ApiErrorContext.WALLET)))
                }
        }
    }

    private fun SavedCard.toUiModel(): CardUiModel =
        CardUiModel(
            id = id,
            brand = brand,
            last4 = last4,
            expiry = "%02d/%02d".format(expMonth, expYear % 100),
            isDefault = isDefault,
        )

    private fun WalletTransaction.toUiModel(): TransactionUiModel {
        val isIncome = amount >= 0
        val signedAmount = if (isIncome) {
            "+${formatAmount(amount)}"
        } else {
            formatAmount(amount)
        }
        return TransactionUiModel(
            id = id,
            title = description.ifBlank { transactionTitle(type) },
            date = dateFormatter.format(createdAt),
            amount = signedAmount,
            isIncome = isIncome,
        )
    }

    private fun transactionTitle(type: String): String =
        when (type.uppercase()) {
            "TOPUP" -> "Bakiye yükleme"
            "RENTAL_PAYMENT" -> "Kiralama ödemesi"
            "REFERRAL_BONUS" -> "Referans bonusu"
            else -> "İşlem"
        }

    private fun formatBalance(balance: Double): String =
        String.format(Locale.forLanguageTag("tr-TR"), "%,.2f ₺", balance)

    private fun formatAmount(amount: Double): String =
        String.format(Locale.forLanguageTag("tr-TR"), "%,.2f ₺", kotlin.math.abs(amount))

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
}
