package com.turkcell.rencarapp.data.network.api

import com.turkcell.rencarapp.data.network.dto.LicenseStatusResponseDto
import retrofit2.http.GET

/**
 * License API iskeleti — OpenAPI /license uçları.
 * Multipart upload Sprint 1'de eklenecektir.
 */
interface LicenseApi {

    @GET("license/status")
    suspend fun getStatus(): LicenseStatusResponseDto
}
