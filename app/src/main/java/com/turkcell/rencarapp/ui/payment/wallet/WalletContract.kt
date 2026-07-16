package com.turkcell.rencarapp.ui.payment.wallet

sealed interface WalletIntent {
    data object FetchInitialData : WalletIntent
    data object AddBalanceClicked : WalletIntent
    data object DismissDepositDialog : WalletIntent
    data class Deposit(val amount: Double) : WalletIntent

    data object AddCardClicked : WalletIntent
    data object DismissCardBottomSheet : WalletIntent
    data class SubmitNewCard(val number: String, val expiry: String) : WalletIntent

    data class DeleteCardClicked(val cardId: String) : WalletIntent
    data class SetDefaultCardClicked(val cardId: String) : WalletIntent
}

sealed interface WalletEffect {
    data class ShowToast(val message: String) : WalletEffect
    data class ShowError(val message: String) : WalletEffect
}

data class WalletUiState(
    val balance: String = "0,00 ₺",
    val isLoading: Boolean = true,
    val isActionLoading: Boolean = false,
    val showDepositDialog: Boolean = false,
    val showCardBottomSheet: Boolean = false,
    val savedCards: List<CardUiModel> = emptyList(),
    val recentTransactions: List<TransactionUiModel> = emptyList(),
    val error: String? = null,
)

data class CardUiModel(
    val id: String,
    val brand: String,
    val last4: String,
    val expiry: String,
    val isDefault: Boolean,
)

data class TransactionUiModel(
    val id: String,
    val title: String,
    val date: String,
    val amount: String,
    val isIncome: Boolean,
)
