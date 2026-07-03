package com.turkcell.rencarapp.ui.payment.wallet

sealed interface WalletIntent {
    data object AddBalanceClicked : WalletIntent
    data object AddCardClicked : WalletIntent
    data class CardClicked(val cardId: String) : WalletIntent
}

sealed interface WalletEffect {
    data class ShowToast(val message: String) : WalletEffect
}

data class WalletUiState(
    val balance: String = "₺0,00",
    val savedCards: List<CardUiModel> = emptyList(),
    val recentTransactions: List<TransactionUiModel> = emptyList()
)

data class CardUiModel(
    val id: String,
    val brand: String, // Örn: "VISA", "MC"
    val last4: String,
    val expiry: String,
    val isDefault: Boolean
)

data class TransactionUiModel(
    val id: String,
    val title: String,
    val date: String,
    val amount: String,
    val isIncome: Boolean // Gelir (Yeşil) veya Gider (Kırmızı)
)