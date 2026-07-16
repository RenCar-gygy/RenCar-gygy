package com.turkcell.rencarapp.data.vehicle

import com.turkcell.rencarapp.data.rental.RentalPlan

enum class VehicleType {
    SEDAN,
    SUV,
    HATCHBACK,
    STATION,
    MINIVAN,
}

enum class VehicleSegment {
    ECONOMY,
    COMFORT,
    SUV,
}

enum class VehicleTransmission {
    MANUAL,
    AUTOMATIC,
}

enum class VehicleStatus {
    AVAILABLE,
    RESERVED,
    RENTED,
    MAINTENANCE,
}

data class Vehicle(
    val id: String,
    val plate: String,
    val brand: String,
    val model: String,
    val type: VehicleType,
    val pricePerDay: Double,
    val pricePerMinute: Double,
    val pricePerHour: Double,
    val fuelPercent: Int,
    val rangeKm: Int,
    val transmission: VehicleTransmission,
    val seats: Int,
    val segment: VehicleSegment,
    val status: VehicleStatus,
    val latitude: Double,
    val longitude: Double,
)

interface VehicleRepository {
    suspend fun listAvailable(
        type: VehicleType? = null,
        segment: VehicleSegment? = null,
        includeBusy: Boolean = false,
    ): Result<List<Vehicle>>

    suspend fun getById(id: String): Result<Vehicle>

    suspend fun getQuote(
        id: String,
        plan: RentalPlan,
        minutes: Int
    ): Result<com.turkcell.rencarapp.data.network.dto.Quote>
}
