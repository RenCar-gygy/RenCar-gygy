package com.turkcell.rencarapp.data.network.api

import com.turkcell.rencarapp.data.network.dto.AuthResponseDto
import com.turkcell.rencarapp.data.network.dto.LoginDto
import com.turkcell.rencarapp.data.network.dto.RegisterDto
import com.turkcell.rencarapp.data.network.dto.UserResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Auth API iskeleti — OpenAPI /auth uçları.
 * Sprint 1'de DefaultAuthRepository bu arayüzü kullanacaktır.
 */
interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterDto): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body body: LoginDto): AuthResponseDto

    @POST("auth/logout")
    suspend fun logout()

    @GET("auth/me")
    suspend fun me(): UserResponseDto
}
