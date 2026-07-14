package com.turkcell.rencarapp.data.network.api

import com.turkcell.rencarapp.data.network.dto.ActiveRentalResponseDto
import com.turkcell.rencarapp.data.network.dto.CreateRentalDto
import com.turkcell.rencarapp.data.network.dto.RentalPhotosState
import com.turkcell.rencarapp.data.network.dto.RentalResponseDto
import com.turkcell.rencarapp.data.network.dto.VehicleResponseDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
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

    @GET("rentals/active")
    suspend fun getActive(
        @Header("Authorization") authorization: String
    ): ActiveRentalResponseDto

    @GET("rentals/{id}")
    suspend fun getById(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): RentalResponseDto

    @POST("rentals/{id}/start")
    suspend fun start(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): RentalResponseDto

    @POST("rentals/{id}/finish")
    suspend fun finish(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): RentalResponseDto

    @POST("rentals/{id}/return")
    suspend fun returnRental(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): RentalResponseDto

    @Multipart
    @POST("rentals/{id}/photos")
    suspend fun uploadPhoto(
        @Header("Authorization") authorization: String,
        @Path("id") id: String,
        @Part side: MultipartBody.Part,
        @Part file: MultipartBody.Part
    ): RentalPhotosState

    @GET("rentals/{id}/photos")
    suspend fun getPhotos(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): RentalPhotosState

    @DELETE("rentals/{id}")
    suspend fun cancel(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    )

    @GET("vehicles/{id}")
    suspend fun getVehicleById(
        @Header("Authorization") authorization: String,
        @Path("id") id: String
    ): VehicleResponseDto
}
