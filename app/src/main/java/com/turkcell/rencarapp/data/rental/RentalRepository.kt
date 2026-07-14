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
    val startDate: Instant,
    val endDate: Instant,
    val totalPrice: Double,
    val status: RentalStatus,
)

data class CreateRentalRequest(
    val vehicleId: String,
    val plan: RentalPlan = RentalPlan.DAILY,
    val endDate: Instant? = null,
)

interface RentalRepository {
    suspend fun create(request: CreateRentalRequest): Result<Rental>
    suspend fun listMine(): Result<List<Rental>>
    suspend fun getById(id: String): Result<Rental>
    suspend fun returnRental(id: String): Result<Rental>
    suspend fun getVehicleById(id: String): Result<VehicleResponseDto>

    suspend fun uploadPhoto(
        rentalId: String,
        side: String,
        imageUri: android.net.Uri
    ): Result<com.turkcell.rencarapp.data.network.dto.RentalPhotosState>

    suspend fun getPhotos(
        rentalId: String
    ): Result<com.turkcell.rencarapp.data.network.dto.RentalPhotosState>

    suspend fun start(
        rentalId: String
    ): Result<Rental>

    suspend fun getActive(): Result<com.turkcell.rencarapp.data.network.dto.ActiveRentalResponseDto>
    suspend fun finish(rentalId: String): Result<Rental>
}