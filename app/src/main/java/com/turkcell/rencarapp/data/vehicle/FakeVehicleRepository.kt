package com.turkcell.rencarapp.data.vehicle

import com.turkcell.rencarapp.data.rental.RentalPlan
import com.turkcell.rencarapp.data.rental.toNetwork
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
            pricePerMinute = 1.04,
            pricePerHour = 62.5,
            fuelPercent = 80,
            rangeKm = 420,
            transmission = VehicleTransmission.AUTOMATIC,
            seats = 5,
            segment = VehicleSegment.COMFORT,
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
            pricePerMinute = 0.63,
            pricePerHour = 37.5,
            fuelPercent = 65,
            rangeKm = 310,
            transmission = VehicleTransmission.MANUAL,
            seats = 5,
            segment = VehicleSegment.ECONOMY,
            status = VehicleStatus.AVAILABLE,
            latitude = 41.0250,
            longitude = 28.9900,
        ),
    )

    override suspend fun listAvailable(
        type: VehicleType?,
        segment: VehicleSegment?,
        includeBusy: Boolean,
    ): Result<List<Vehicle>> {
        delay(FAKE_DELAY_MS)
        val filtered = vehicles.filter { vehicle ->
            (includeBusy || vehicle.status == VehicleStatus.AVAILABLE) &&
                (type == null || vehicle.type == type) &&
                (segment == null || vehicle.segment == segment)
        }
        return Result.success(filtered)
    }

    override suspend fun getById(id: String): Result<Vehicle> {
        delay(FAKE_DELAY_MS)
        return vehicles.find { it.id == id && it.status == VehicleStatus.AVAILABLE }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("Araç bulunamadı veya müsait değil."))
    }

    override suspend fun getQuote(
        id: String,
        plan: RentalPlan,
        minutes: Int
    ): Result<com.turkcell.rencarapp.data.network.dto.Quote> {
        delay(FAKE_DELAY_MS)
        return Result.success(
            com.turkcell.rencarapp.data.network.dto.Quote(
                vehicleId = id,
                plan = plan.toNetwork(),
                minutes = minutes,
                usageFee = 135.0,
                startFee = 15.0,
                serviceFee = 7.0,
                estimatedTotal = 157.0
            )
        )
    }

    private companion object {
        const val FAKE_DELAY_MS = 400L
    }
}
