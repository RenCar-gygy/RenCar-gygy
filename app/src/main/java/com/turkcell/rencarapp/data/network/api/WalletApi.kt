package com.turkcell.rencarapp.data.network.api

import com.turkcell.rencarapp.data.network.dto.TopupDto
import com.turkcell.rencarapp.data.network.dto.WalletResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Wallet API — OpenAPI /wallet uçları.
 * Tek kaynak: https://rencarv2.halitkalayci.com/api/openapi.json
 */
interface WalletApi {

    @GET("wallet")
    suspend fun getWallet(
        @Header("Authorization") authorization: String,
    ): WalletResponseDto

    @POST("wallet/topup")
    suspend fun topup(
        @Header("Authorization") authorization: String,
        @Body body: TopupDto,
    ): WalletResponseDto
}
