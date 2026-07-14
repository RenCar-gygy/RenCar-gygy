package com.turkcell.rencarapp.data.network.api

import com.turkcell.rencarapp.data.network.dto.Quote
import com.turkcell.rencarapp.data.network.dto.VehicleResponseDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Vehicle API — OpenAPI /vehicles uçları.
 * Tek kaynak: https://rencarv2.halitkalayci.com/api/openapi.json
 */
interface VehicleApi {

    @GET("vehicles")
    suspend fun listAvailable(
        @Header("Authorization") authorization: String,
        @Query("type") type: String? = null,
        @Query("segment") segment: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("includeBusy") includeBusy: String? = null,
    ): List<VehicleResponseDto>

    @GET("vehicles/{id}")
    suspend fun getById(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
    ): VehicleResponseDto

    @GET("vehicles/{id}/quote")
    suspend fun getQuote(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
        @Query("plan") plan: String,
        @Query("minutes") minutes: Int
    ): Quote
}
