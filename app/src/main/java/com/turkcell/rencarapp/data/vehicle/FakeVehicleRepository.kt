package com.turkcell.rencarapp.data.vehicle

import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeVehicleRepository @Inject constructor() : VehicleRepository {

    private val vehicles = listOf(
        Vehicle(
            id = "veh-1",
            plate = "34 ABC 123",
            brand = "Volkswagen",
            model = "Passat",
            type = VehicleType.SEDAN,
            pricePerDay = 1500.0,
            status = VehicleStatus.AVAILABLE,
            latitude = 41.0151,
            longitude = 28.9795,
        ),
        Vehicle(
            id = "veh-2",
            plate = "06 XYZ 456",
            brand = "Renault",
            model = "Clio",
            type = VehicleType.HATCHBACK,
            pricePerDay = 900.0,
            status = VehicleStatus.AVAILABLE,
            latitude = 41.0250,
            longitude = 28.9900,
        ),
    )

    override suspend fun listAvailable(type: VehicleType?): Result<List<Vehicle>> {
        delay(FAKE_DELAY_MS)
        val filtered = vehicles.filter { vehicle ->
            vehicle.status == VehicleStatus.AVAILABLE &&
                (type == null || vehicle.type == type)
        }
        return Result.success(filtered)
    }

    override suspend fun getById(id: String): Result<Vehicle> {
        delay(FAKE_DELAY_MS)
        return vehicles.find { it.id == id && it.status == VehicleStatus.AVAILABLE }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("Araç bulunamadı veya müsait değil."))
    }

    private companion object {
        const val FAKE_DELAY_MS = 400L
    }
}
