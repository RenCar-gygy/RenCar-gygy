package com.turkcell.rencarapp.ui.payment.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    private val _effect = Channel<WalletEffect>(Channel.BUFFERED)
    val effect: Flow<WalletEffect> = _effect.receiveAsFlow()

    init {
        loadStubData()
    }

    fun onIntent(intent: WalletIntent) {
        when (intent) {
            is WalletIntent.AddBalanceClicked -> {
                viewModelScope.launch { _effect.send(WalletEffect.ShowToast("Bakiye yükleme ekranı henüz aktif değil (Stub)")) }
            }
            is WalletIntent.AddCardClicked -> {
                viewModelScope.launch { _effect.send(WalletEffect.ShowToast("Kart ekleme ekranı henüz aktif değil (Stub)")) }
            }
            is WalletIntent.CardClicked -> {
                viewModelScope.launch { _effect.send(WalletEffect.ShowToast("Kart detayına tıklanıldı (Stub)")) }
            }
        }
    }

    private fun loadStubData() {
        // Tasarımda görünen sabit verileri state'e basıyoruz
        _uiState.value = WalletUiState(
            balance = "₺340,00",
            savedCards = listOf(
                CardUiModel(id = "1", brand = "VISA", last4 = "4291", expiry = "08/27", isDefault = true),
                CardUiModel(id = "2", brand = "MC", last4 = "7740", expiry = "11/26", isDefault = false)
            ),
            recentTransactions = listOf(
                TransactionUiModel(id = "t1", title = "Renault Clio kiralama", date = "Bugün - 14:32", amount = "-₺110,50", isIncome = false),
                TransactionUiModel(id = "t2", title = "Bakiye yükleme", date = "Dün - 09:10", amount = "+₺200,00", isIncome = true)
            )
        )
    }
}