package com.turkcell.rencarapp.data.network.dto

data class HealthResponse(
    val status: String? = null
)


data class DirectPaymentRequest(
    val price: String,
    val paidPrice: String,
    val installment: Int = 1,
    val paymentCard: PaymentCard,
    val buyer: Buyer,
    val shippingAddress: Address,
    val billingAddress: Address,
    val basketItems: List<BasketItem>
)

data class PaymentCard(
    val cardHolderName: String,
    val cardNumber: String,
    val expireMonth: String,
    val expireYear: String,
    val cvc: String,
    val registerCard: Int = 0
)

data class Buyer(
    val id: String,
    val name: String,
    val surname: String,
    val gsmNumber: String,
    val email: String,
    val identityNumber: String,
    val lastLoginDate: String? = null,
    val registrationDate: String? = null,
    val registrationAddress: String,
    val ip: String,
    val city: String,
    val country: String,
    val zipCode: String
)

data class Address(
    val contactName: String,
    val city: String,
    val country: String,
    val address: String,
    val zipCode: String? = null
)

data class BasketItem(
    val id: String,
    val name: String,
    val category1: String,
    val itemType: String = "VIRTUAL",
    val price: String
)


data class PaymentResponse(
    val status: String? = null,
    val paymentId: String? = null,
    val errorMessage: String? = null
)

data class ThreeDSInitializeRequest(
    val price: String? = null
)

data class ThreeDSInitializeResponse(
    val status: String? = null,
    val htmlContent: String? = null
)

data class CheckoutFormRequest(
    val price: String? = null
)

data class CheckoutFormInitializeResponse(
    val status: String? = null,
    val paymentPageUrl: String? = null
)

data class CheckoutFormResultResponse(
    val status: String? = null,
    val paymentStatus: String? = null
)

data class PaymentStatusResponse(
    val status: String? = null
)