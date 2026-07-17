package com.turkcell.rencarapp.data.payment

import com.turkcell.rencarapp.data.network.dto.*

interface IyzicoRepository {
    suspend fun checkHealth(): Result<HealthResponse>
    suspend fun processDirectPayment(request: DirectPaymentRequest): Result<PaymentResponse>
    suspend fun initializeCheckoutForm(request: CheckoutFormRequest): Result<CheckoutFormInitializeResponse>
}