package com.turkcell.rencarapp.data.network.api

import com.turkcell.rencarapp.data.network.dto.CardResponseDto
import com.turkcell.rencarapp.data.network.dto.CreateCardDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Card API — OpenAPI /cards uçları.
 * Tek kaynak: https://rencarv2.halitkalayci.com/api/openapi.json
 */
interface CardApi {

    @GET("cards")
    suspend fun listCards(
        @Header("Authorization") authorization: String,
    ): List<CardResponseDto>

    @POST("cards")
    suspend fun addCard(
        @Header("Authorization") authorization: String,
        @Body body: CreateCardDto,
    ): CardResponseDto

    @DELETE("cards/{id}")
    suspend fun deleteCard(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
    )

    @PATCH("cards/{id}/default")
    suspend fun setDefaultCard(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
    ): CardResponseDto
}
