package com.turkcell.rencarapp.data.vehicle

enum class VehicleType {
    SEDAN,
    SUV,
    HATCHBACK,
    STATION,
    MINIVAN,
}

enum class VehicleStatus {
    AVAILABLE,
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
    val status: VehicleStatus,
    val latitude: Double,
    val longitude: Double,
)

interface VehicleRepository {
    suspend fun listAvailable(type: VehicleType? = null): Result<List<Vehicle>>

    suspend fun getById(id: String): Result<Vehicle>
}
