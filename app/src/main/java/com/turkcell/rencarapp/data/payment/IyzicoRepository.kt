package com.turkcell.rencarapp.data.payment

import com.turkcell.rencarapp.data.network.dto.CheckoutFormInitializeResponseDto
import com.turkcell.rencarapp.data.network.dto.IyzicoPaymentResponseDto
import com.turkcell.rencarapp.data.network.dto.InitializeCheckoutFormDto

interface IyzicoRepository {
    suspend fun initializeCheckoutForm(
        request: InitializeCheckoutFormDto,
    ): Result<CheckoutFormInitializeResponseDto>

    suspend fun getCheckoutFormResult(token: String): Result<IyzicoPaymentResponseDto>
}
