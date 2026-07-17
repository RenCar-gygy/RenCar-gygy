package com.turkcell.rencarapp.data.payment

import com.turkcell.rencarapp.data.network.api.IyzicoApi
import com.turkcell.rencarapp.data.network.dto.*
import javax.inject.Inject

class DefaultIyzicoRepository @Inject constructor(
    private val api: IyzicoApi
) : IyzicoRepository {

    override suspend fun checkHealth(): Result<HealthResponse> {
        return try {
            val response = api.checkHealth()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Sunucu hatası: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun processDirectPayment(request: DirectPaymentRequest): Result<PaymentResponse> {
        return try {
            val response = api.processDirectPayment(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Ödeme işlemi başarısız: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun initializeCheckoutForm(request: CheckoutFormRequest): Result<CheckoutFormInitializeResponse> {
        return try {
            val response = api.initializeCheckoutForm(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Form başlatılamadı: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}