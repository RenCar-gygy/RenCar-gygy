package com.turkcell.rencarapp.data.rental

import com.turkcell.rencarapp.data.network.dto.VehicleResponseDto
import java.time.Instant

enum class RentalStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED,
    PREPARING
}

enum class RentalPlan {
    PER_MINUTE,
    HOURLY,
    DAILY
}

data class Rental(
    val id: String,
    val userId: String,
    val vehicleId: String,
    val plan: RentalPlan,
    val startDate: Instant,
    val endDate: Instant?,
    val totalPrice: Double,
    val startFee: Double,
    val serviceFee: Double,
    val distanceKm: Double,
    val durationMinutes: Int,
    val status: RentalStatus,
    val paymentStatus: String,
    val discountAmount: Double = 0.0,
    val paymentMethod: String? = null,
    val vehicleBrand: String,
    val vehicleModel: String,
    val vehiclePlate: String,
)

data class CreateRentalRequest(
    val vehicleId: String,
    val plan: RentalPlan = RentalPlan.DAILY,
    val endDate: Instant? = null,
)

enum class PaymentMethod {
    WALLET,
    CARD,
    IYZICO,
}

data class PayRentalRequest(
    val method: PaymentMethod,
    val cardId: String? = null,
    val discountCode: String? = null,
    val iyzicoPaymentId: String? = null,
)

data class PayRentalResult(
    val rentalId: String,
    val paymentStatus: String,
    val method: String,
    val totalPrice: Double,
    val discountAmount: Double,
    val paidAmount: Double,
    val walletBalance: Double?,
    val cardBrand: String?,
    val cardLast4: String?,
)

data class RentalStats(
    val month: String,
    val tripCount: Int,
    val totalSpent: Double,
    val totalMinutes: Int,
    val totalKm: Double,
)

interface RentalRepository {
    suspend fun create(request: CreateRentalRequest): Result<Rental>
    suspend fun listMine(): Result<List<Rental>>
    suspend fun getById(id: String): Result<Rental>
    suspend fun getVehicleById(id: String): Result<VehicleResponseDto>

    suspend fun uploadPhoto(
        rentalId: String,
        side: String,
        imageUri: android.net.Uri
    ): Result<com.turkcell.rencarapp.data.network.dto.RentalPhotosState>

    suspend fun uploadPhotoBytes(
        rentalId: String,
        side: String,
        bytes: ByteArray
    ): Result<com.turkcell.rencarapp.data.network.dto.RentalPhotosState>

    suspend fun getPhotos(
        rentalId: String
    ): Result<com.turkcell.rencarapp.data.network.dto.RentalPhotosState>

    suspend fun start(
        rentalId: String
    ): Result<Rental>

    suspend fun getActive(): Result<com.turkcell.rencarapp.data.network.dto.ActiveRentalResponseDto>
    suspend fun finish(rentalId: String): Result<Rental>
    suspend fun cancel(rentalId: String): Result<Unit>
    suspend fun pay(rentalId: String, request: PayRentalRequest): Result<PayRentalResult>
    suspend fun getStats(): Result<RentalStats>
}
