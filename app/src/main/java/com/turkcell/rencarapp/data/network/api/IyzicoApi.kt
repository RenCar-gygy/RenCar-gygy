package com.turkcell.rencarapp.data.network.api

import com.turkcell.rencarapp.data.network.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface IyzicoApi {

    @GET("/iyzico/health")
    suspend fun checkHealth(): Response<HealthResponse>

    @POST("/iyzico/payments")
    suspend fun processDirectPayment(
        @Body request: DirectPaymentRequest
    ): Response<PaymentResponse>

    @POST("/iyzico/payments/threeds/initialize")
    suspend fun initialize3DSPayment(
        @Body request: ThreeDSInitializeRequest
    ): Response<ThreeDSInitializeResponse>

    @POST("/iyzico/checkout-form/initialize")
    suspend fun initializeCheckoutForm(
        @Body request: CheckoutFormRequest
    ): Response<CheckoutFormInitializeResponse>

    @GET("/iyzico/checkout-form/result/{token}")
    suspend fun getCheckoutFormResult(
        @Path("token") token: String
    ): Response<CheckoutFormResultResponse>

    @GET("/iyzico/payments/{paymentId}")
    suspend fun getPaymentStatus(
        @Path("paymentId") paymentId: String
    ): Response<PaymentStatusResponse>
}