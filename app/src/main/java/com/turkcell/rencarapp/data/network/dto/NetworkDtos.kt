package com.turkcell.rencarapp.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterDto(
    val email: String,
    val password: String,
    val fullName: String,
    val phone: String,
    val referralCode: String? = null,
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
    val referralCode: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
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
    val frontImageUrl: String? = null,
    val backImageUrl: String? = null,
    val rejectReason: String? = null,
    val reviewedAt: String? = null,
)

@Serializable
data class VehicleResponseDto(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val type: String,
    val pricePerDay: Double,
    val pricePerMinute: Double,
    val pricePerHour: Double,
    val fuelPercent: Int,
    val rangeKm: Int,
    val transmission: String,
    val seats: Int,
    val segment: String,
    val status: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: String? = null,
    val updatedAt: String? = null,
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
