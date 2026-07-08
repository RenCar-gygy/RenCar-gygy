package com.turkcell.rencarapp.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterDto(
    val email: String,
    val password: String,
    val fullName: String,
    val phone: String? = null,
)

@Serializable
data class LoginDto(
    val phone: String,
)

@Serializable
data class VerifyOtpDto(
    val phone: String,
    val code: String,
)

@Serializable
data class OtpRequiredResponseDto(
    val message: String,
    val phone: String,
    val expiresAt: String? = null,
)

@Serializable
data class UserResponseDto(
    val id: String,
    val email: String,
    val fullName: String,
    val phone: String? = null,
    val role: String,
)

@Serializable
data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponseDto,
)

@Serializable
data class RefreshTokenDto(
    val refreshToken: String,
)

@Serializable
data class LicenseStatusResponseDto(
    val status: String,
    val rejectReason: String? = null,
)

@Serializable
data class VehicleResponseDto(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val type: String,
    val pricePerDay: Double,
    val status: String,
    val latitude: Double,
    val longitude: Double,
)

@Serializable
data class CreateRentalDto(
    val vehicleId: String,
    val endDate: String,
)

@Serializable
data class RentalResponseDto(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val startDate: String,
    val endDate: String,
    val totalPrice: Double,
    val status: String,
)
