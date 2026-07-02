package com.turkcell.rencarapp.data.network.api

import com.turkcell.rencarapp.data.network.dto.VehicleResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Vehicle API iskeleti — OpenAPI /vehicles uçları.
 */
interface VehicleApi {

    @GET("vehicles")
    suspend fun listAvailable(
        @Query("type") type: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
    ): List<VehicleResponseDto>

    @GET("vehicles/{id}")
    suspend fun getById(@Path("id") id: String): VehicleResponseDto
}
