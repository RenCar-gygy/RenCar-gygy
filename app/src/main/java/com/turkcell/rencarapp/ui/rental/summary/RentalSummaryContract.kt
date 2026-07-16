package com.turkcell.rencarapp.ui.rental.summary

import com.turkcell.rencarapp.data.rental.PaymentMethod

sealed interface RentalSummaryIntent {
    data object PayClicked : RentalSummaryIntent
    data object ChangePaymentMethodClicked : RentalSummaryIntent
    data object DismissPaymentMethodSheet : RentalSummaryIntent
    data object SelectWalletPayment : RentalSummaryIntent
    data class SelectCardPayment(val cardId: String) : RentalSummaryIntent
    data class DiscountCodeChanged(val code: String) : RentalSummaryIntent
    data object NavigateHomeClicked : RentalSummaryIntent
}

sealed interface RentalSummaryEffect {
    data object NavigateToHome : RentalSummaryEffect
    data class ShowToast(val message: String) : RentalSummaryEffect
    data class ShowError(val message: String) : RentalSummaryEffect
}

data class PaymentCardOption(
    val id: String,
    val brand: String,
    val last4: String,
    val isDefault: Boolean,
)

data class RentalSummaryUiState(
    val isLoading: Boolean = false,
    val isPaying: Boolean = false,
    val rentalId: String = "",
    val vehicleName: String = "",
    val plate: String = "",
    val durationText: String = "",
    val distanceText: String = "",
    val rentalFee: String = "",
    val startFee: String = "",
    val serviceFee: String = "",
    val discount: String = "",
    val totalFee: String = "",
    val isPaid: Boolean = false,
    val paymentMethodLabel: String = "",
    val cardBrand: String = "",
    val cardLast4: String = "",
    val walletBalance: String? = null,
    val walletBalanceAmount: Double? = null,
    val payableAmount: Double = 0.0,
    val selectedMethod: PaymentMethod = PaymentMethod.WALLET,
    val selectedCardId: String? = null,
    val availableCards: List<PaymentCardOption> = emptyList(),
    val showPaymentMethodSheet: Boolean = false,
    val discountCodeInput: String = "",
)
