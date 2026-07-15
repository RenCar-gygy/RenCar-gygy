package com.turkcell.rencarapp.data.wallet

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import javax.inject.Inject

// --- DTOs (Veri Transfer Objeleri) ---
data class WalletResponse(val id: String?, val balance: Double = 0.0)
data class DepositRequest(val amount: Double)

data class CardResponse(val id: String, val cardHolderName: String, val cardNumber: String, val expireDate: String, val brand: String = "VISA")
data class AddCardRequest(val cardHolderName: String, val cardNumber: String, val expireDate: String)

// --- API INTERFACES (Hocanın istediği v2 Güncellemesi yapıldı) ---
interface WalletApi {
    @GET("api/v2/wallets/me") // TAMAMEN v2 YAPILDI
    suspend fun getMyWallet(): WalletResponse

    @POST("api/v2/wallets/deposit") // TAMAMEN v2 YAPILDI
    suspend fun deposit(@Body request: DepositRequest): WalletResponse
}

interface CardApi {
    @GET("api/v2/cards") // TAMAMEN v2 YAPILDI
    suspend fun getMyCards(): List<CardResponse>

    @POST("api/v2/cards") // TAMAMEN v2 YAPILDI
    suspend fun addCard(@Body request: AddCardRequest): CardResponse
}

// --- REPOSITORY (MVI ve Clean Architecture Standartlarında) ---
class WalletAndCardRepository @Inject constructor(
    private val walletApi: WalletApi,
    private val cardApi: CardApi
) {
    suspend fun getMyWallet() = runCatching { walletApi.getMyWallet() }
    suspend fun deposit(amount: Double) = runCatching { walletApi.deposit(DepositRequest(amount)) }

    suspend fun getMyCards() = runCatching { cardApi.getMyCards() }
    suspend fun addCard(req: AddCardRequest) = runCatching { cardApi.addCard(req) }
}