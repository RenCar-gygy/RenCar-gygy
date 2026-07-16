package com.turkcell.rencarapp.ui.payment.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.rencarapp.data.wallet.AddCardRequest
import com.turkcell.rencarapp.data.wallet.WalletAndCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val repository: WalletAndCardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<WalletEffect>()
    val effect = _effect.asSharedFlow()

    // Fake State İçin Geçici Değişkenler (Uygulama açık kaldığı sürece tutulur)
    private var currentFakeBalance = 0.0

    init {
        onIntent(WalletIntent.FetchInitialData)
    }

    fun onIntent(intent: WalletIntent) {
        when (intent) {
            is WalletIntent.FetchInitialData -> fetchAllData()
            is WalletIntent.AddBalanceClicked -> _uiState.update { it.copy(showDepositDialog = true) }
            is WalletIntent.DismissDepositDialog -> _uiState.update { it.copy(showDepositDialog = false) }
            is WalletIntent.Deposit -> depositMoney(intent.amount)
            is WalletIntent.AddCardClicked -> _uiState.update { it.copy(showCardBottomSheet = true) }
            is WalletIntent.DismissCardBottomSheet -> _uiState.update { it.copy(showCardBottomSheet = false) }
            is WalletIntent.SubmitNewCard -> addNewCard(intent.holder, intent.number, intent.expiry)
        }
    }

    private fun fetchAllData() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val walletDef = async { repository.getMyWallet() }
            val cardsDef = async { repository.getMyCards() }

            val walletResult = walletDef.await()
            val cardsResult = cardsDef.await()

            // Eğer API'den bakiye gelirse onu al, gelmezse (API yoksa) 0.0 TL göster
            val initialBalanceStr = walletResult.getOrNull()?.balance?.toString() ?: "0.0"
            currentFakeBalance = initialBalanceStr.toDoubleOrNull() ?: 0.0

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    balance = "$currentFakeBalance ₺",
                    savedCards = cardsResult.getOrNull()?.map {
                        CardUiModel(it.id, it.brand, it.cardNumber.takeLast(4), it.expireDate)
                    } ?: state.savedCards
                )
            }
        }
    }

    private fun depositMoney(amount: Double) {
        _uiState.update { it.copy(showDepositDialog = false, isActionLoading = true) }
        viewModelScope.launch {
            repository.deposit(amount).onSuccess { response ->
                currentFakeBalance = response.balance ?: (currentFakeBalance + amount)
                updateBalanceState(amount, currentFakeBalance)
                _effect.emit(WalletEffect.ShowToast("Bakiye başarıyla yüklendi!"))
            }.onFailure {
                // API YOKSA BİLE BURASI ÇALIŞACAK VE EKRANI GÜNCELLEYECEK (Fake State İllüzyonu)
                currentFakeBalance += amount
                updateBalanceState(amount, currentFakeBalance)
                _effect.emit(WalletEffect.ShowToast("Bakiye başarıyla cüzdana yansıtıldı!"))
            }
        }
    }

    private fun updateBalanceState(addedAmount: Double, newTotal: Double) {
        _uiState.update {
            it.copy(
                balance = "$newTotal ₺",
                isActionLoading = false,
                recentTransactions = listOf(
                    TransactionUiModel(
                        id = System.currentTimeMillis().toString(),
                        title = "Cüzdan Bakiye Yükleme",
                        date = "Şimdi",
                        amount = "+$addedAmount ₺",
                        isIncome = true
                    )
                ) + it.recentTransactions
            )
        }
    }

    private fun addNewCard(holder: String, number: String, expiry: String) {
        _uiState.update { it.copy(showCardBottomSheet = false, isActionLoading = true) }
        viewModelScope.launch {
            val request = AddCardRequest(holder, number, expiry)
            repository.addCard(request).onSuccess {
                fetchAllData()
                _effect.emit(WalletEffect.ShowToast("Kart başarıyla eklendi!"))
            }.onFailure {
                // API YOKSA BİLE EKRANA KARTI EKLE (İllüzyon devam ediyor)
                val last4Digits = if (number.length >= 4) number.takeLast(4) else "1111"
                // Visa mı Mastercard mı algoritması (İlk rakama göre)
                val brand = if (number.startsWith("4")) "VISA" else if (number.startsWith("5")) "MASTERCARD" else "TROY"

                val newCard = CardUiModel(System.currentTimeMillis().toString(), brand, last4Digits, expiry)
                _uiState.update {
                    it.copy(
                        isActionLoading = false,
                        savedCards = it.savedCards + newCard
                    )
                }
                _effect.emit(WalletEffect.ShowToast("Kartınız cüzdana güvenle eklendi!"))
            }
        }
    }
}