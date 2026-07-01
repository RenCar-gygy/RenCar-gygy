package com.turkcell.rencarapp.data.rental

import java.time.Instant

enum class RentalStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED,
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
    val endDate: Instant,
)

interface RentalRepository {
    suspend fun create(request: CreateRentalRequest): Result<Rental>

    suspend fun listMine(): Result<List<Rental>>

    suspend fun getById(id: String): Result<Rental>

    suspend fun returnRental(id: String): Result<Rental>
}
