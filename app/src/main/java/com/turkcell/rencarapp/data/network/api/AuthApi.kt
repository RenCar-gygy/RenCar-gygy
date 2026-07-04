package com.turkcell.rencarapp.data.network.api

import com.turkcell.rencarapp.data.network.dto.AuthResponseDto
import com.turkcell.rencarapp.data.network.dto.LoginDto
import com.turkcell.rencarapp.data.network.dto.OtpRequiredResponseDto
import com.turkcell.rencarapp.data.network.dto.RegisterDto
import com.turkcell.rencarapp.data.network.dto.UserResponseDto
import com.turkcell.rencarapp.data.network.dto.VerifyOtpDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Auth API — OpenAPI /auth uçları.
 * Tek kaynak: https://rencar.halitkalayci.com/api/docs-json
 */
interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterDto): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body body: LoginDto): OtpRequiredResponseDto

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body body: VerifyOtpDto): AuthResponseDto

    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") authorization: String)

    @GET("auth/me")
    suspend fun me(@Header("Authorization") authorization: String): UserResponseDto
}
