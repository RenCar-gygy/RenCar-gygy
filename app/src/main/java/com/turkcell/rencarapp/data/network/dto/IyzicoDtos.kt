
package com.turkcell.rencarapp.data.network.dto

data class HealthResponse(
    val status: String? = null
)

data class DirectPaymentRequest(
    val price: String? = null,
)

data class PaymentResponse(
    val status: String? = null
)

data class ThreeDSInitializeRequest(
    val price: String? = null
)

data class ThreeDSInitializeResponse(
    val status: String? = null
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