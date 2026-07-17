package com.turkcell.rencarapp.data.network.api

import com.turkcell.rencarapp.data.network.dto.CheckoutFormInitializeResponseDto
import com.turkcell.rencarapp.data.network.dto.HealthResponse
import com.turkcell.rencarapp.data.network.dto.InitializeCheckoutFormDto
import com.turkcell.rencarapp.data.network.dto.IyzicoPaymentResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Iyzico API — OpenAPI /iyzico uçları.
 * Tek kaynak: https://rencarv2.halitkalayci.com/api/openapi.json
 */
interface IyzicoApi {

    @GET("iyzico/health")
    suspend fun checkHealth(): HealthResponse

    @POST("iyzico/checkout-form/initialize")
    suspend fun initializeCheckoutForm(
        @Header("Authorization") authorization: String,
        @Body request: InitializeCheckoutFormDto,
    ): CheckoutFormInitializeResponseDto

    @GET("iyzico/checkout-form/result/{token}")
    suspend fun getCheckoutFormResult(
        @Header("Authorization") authorization: String,
        @Path("token") token: String,
    ): IyzicoPaymentResponseDto
}
