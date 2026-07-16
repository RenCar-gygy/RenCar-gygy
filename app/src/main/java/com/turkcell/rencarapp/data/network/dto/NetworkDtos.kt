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
enum class RentalPlan {
    PER_MINUTE,
    HOURLY,
    DAILY
}

@Serializable
data class CreateRentalDto(
    val vehicleId: String,
    val plan: RentalPlan = RentalPlan.DAILY,
    val endDate: String? = null,
)

@Serializable
data class RentalVehicleSummaryDto(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val type: String,
)

@Serializable
data class RentalResponseDto(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val vehicle: RentalVehicleSummaryDto,
    val plan: RentalPlan,
    val startedAt: String,
    val endedAt: String? = null,
    val endDate: String? = null,
    val totalPrice: Double? = null,
    val startFee: Double,
    val serviceFee: Double? = null,
    val distanceKm: Double,
    val durationMinutes: Int,
    val status: String,
    val paymentStatus: String,
    val paymentMethod: String? = null,
    val discountAmount: Double,
    val createdAt: String,
)

@Serializable
data class ActiveRentalResponseDto(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val vehicle: RentalVehicleSummaryDto,
    val plan: RentalPlan,
    val startedAt: String,
    val endedAt: String? = null,
    val endDate: String? = null,
    val totalPrice: Double? = null,
    val startFee: Double,
    val serviceFee: Double? = null,
    val distanceKm: Double,
    val durationMinutes: Int,
    val status: String,
    val paymentStatus: String,
    val paymentMethod: String? = null,
    val discountAmount: Double,
    val createdAt: String,
    val elapsedSeconds: Int,
    val currentCost: Double,
)

@Serializable
data class RentalPhotoDto(
    val side: String,
    val imageUrl: String,
    val createdAt: String,
)

@Serializable
data class RentalPhotosState(
    val rentalId: String,
    val photos: List<RentalPhotoDto>,
    val uploadedCount: Int,
    val remainingSides: List<String>,
    val photosComplete: Boolean,
)

@Serializable
data class Quote(
    val vehicleId: String,
    val plan: RentalPlan,
    val minutes: Int,
    val usageFee: Double,
    val startFee: Double,
    val serviceFee: Double,
    val estimatedTotal: Double,
)

@Serializable
data class CreateReservationDto(
    val vehicleId: String,
)

@Serializable
data class ReservationVehicleSummaryDto(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val pricePerMinute: Double,
)

@Serializable
data class Reservation(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val vehicle: ReservationVehicleSummaryDto,
    val status: String,
    val expiresAt: String,
    val remainingSeconds: Int,
    val createdAt: String,
)

@Serializable
data class WalletTransactionDto(
    val id: String,
    val type: String,
    val amount: Double,
    val rentalId: String? = null,
    val description: String,
    val createdAt: String,
)

@Serializable
data class WalletResponseDto(
    val id: String,
    val balance: Double,
    val transactions: List<WalletTransactionDto>,
)

@Serializable
data class TopupDto(
    val amount: Double,
)

@Serializable
data class CardResponseDto(
    val id: String,
    val brand: String,
    val last4: String,
    val expMonth: Int,
    val expYear: Int,
    val isDefault: Boolean,
    val createdAt: String,
)

@Serializable
data class CreateCardDto(
    val brand: String,
    val last4: String,
    val expMonth: Int,
    val expYear: Int,
)

@Serializable
data class PayRentalDto(
    val method: String,
    val cardId: String? = null,
    val discountCode: String? = null,
)

@Serializable
data class PaidCardSummaryDto(
    val brand: String,
    val last4: String,
)

@Serializable
data class PayRentalResponseDto(
    val rentalId: String,
    val paymentStatus: String,
    val method: String,
    val totalPrice: Double,
    val discountAmount: Double,
    val paidAmount: Double,
    val walletBalance: Double? = null,
    val card: PaidCardSummaryDto? = null,
)

@Serializable
data class RentalStatsResponseDto(
    val month: String,
    val tripCount: Int,
    val totalSpent: Double,
    val totalMinutes: Int,
    val totalKm: Double,
)
