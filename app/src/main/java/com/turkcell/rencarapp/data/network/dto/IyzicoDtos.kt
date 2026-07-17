package com.turkcell.rencarapp.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    val status: String? = null,
)

@Serializable
data class InitializeCheckoutFormDto(
    val price: Double,
    val description: String? = null,
    val basketId: String? = null,
    val enabledInstallments: List<Int>? = null,
)

@Serializable
data class CheckoutFormInitializeResponseDto(
    val status: String,
    val token: String,
    val tokenExpireTime: Long? = null,
    val paymentPageUrl: String? = null,
    val checkoutFormContent: String? = null,
)

@Serializable
data class IyzicoPaymentResponseDto(
    val status: String,
    val paymentId: String? = null,
    val paymentStatus: String? = null,
    val token: String? = null,
)
