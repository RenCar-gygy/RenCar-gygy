package com.turkcell.rencarapp.data.network.api

import com.turkcell.rencarapp.data.network.dto.CreateRentalDto
import com.turkcell.rencarapp.data.network.dto.RentalResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface RentalApi {

    @POST("rentals")
    suspend fun create(
        @Header("Authorization") authorization: String,
        @Body body: CreateRentalDto
    ): RentalResponseDto

    @GET("rentals")
    suspend fun listMine(
        @Header("Authorization") authorization: String
    ): List<RentalResponseDto>

    @GET("rentals/{id}")
    suspend fun getById(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): RentalResponseDto

    @POST("rentals/{id}/return")
    suspend fun returnRental(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): RentalResponseDto
}