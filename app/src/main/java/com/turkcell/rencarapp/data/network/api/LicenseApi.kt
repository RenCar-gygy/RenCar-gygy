package com.turkcell.rencarapp.data.network.api

import com.turkcell.rencarapp.data.network.dto.LicenseStatusResponseDto
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * License API — OpenAPI /license uçları.
 * Tek kaynak: https://rencarv2.halitkalayci.com/api/openapi.json
 */
interface LicenseApi {

    @GET("license/status")
    suspend fun getStatus(
        @Header("Authorization") authorization: String,
    ): LicenseStatusResponseDto

    @Multipart
    @POST("license/upload")
    suspend fun upload(
        @Header("Authorization") authorization: String,
        @Part front: MultipartBody.Part,
        @Part back: MultipartBody.Part,
        @Part selfie: MultipartBody.Part,
    ): LicenseStatusResponseDto
}
