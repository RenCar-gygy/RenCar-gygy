package com.turkcell.rencarapp.data.network.api

import com.turkcell.rencarapp.data.network.dto.CreateReservationDto
import com.turkcell.rencarapp.data.network.dto.Reservation
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ReservationApi {

    @POST("reservations")
    suspend fun create(
        @Header("Authorization") authorization: String,
        @Body body: CreateReservationDto
    ): Reservation

    @GET("reservations/active")
    suspend fun getActive(
        @Header("Authorization") authorization: String
    ): Reservation

    @DELETE("reservations/{id}")
    suspend fun cancel(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    )
}
