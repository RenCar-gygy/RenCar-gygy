package com.turkcell.rencarapp.data.vehicle

import com.turkcell.rencarapp.data.auth.AuthorizedRequestExecutor
import com.turkcell.rencarapp.data.network.api.VehicleApi
import com.turkcell.rencarapp.data.network.dto.VehicleResponseDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultVehicleRepository @Inject constructor(
    private val vehicleApi: VehicleApi,
    private val authorizedRequestExecutor: AuthorizedRequestExecutor,
) : VehicleRepository {

    override suspend fun listAvailable(
        type: VehicleType?,
        segment: VehicleSegment?,
        includeBusy: Boolean,
    ): Result<List<Vehicle>> =
        authorizedCall { authorization ->
            vehicleApi.listAvailable(
                authorization = authorization,
                type = type?.name,
                segment = segment?.name,
                limit = DEFAULT_PAGE_LIMIT,
                includeBusy = if (includeBusy) "true" else null,
            ).map { it.toDomain() }
        }

    override suspend fun getById(id: String): Result<Vehicle> =
        authorizedCall { authorization ->
            vehicleApi.getById(authorization = authorization, id = id).toDomain()
        }

    override suspend fun getQuote(
        id: String,
        plan: com.turkcell.rencarapp.data.network.dto.RentalPlan,
        minutes: Int
    ): Result<com.turkcell.rencarapp.data.network.dto.Quote> =
        authorizedCall { authorization ->
            vehicleApi.getQuote(
                authorization = authorization,
                id = id,
                plan = plan.name,
                minutes = minutes
            )
        }

    private suspend fun <T> authorizedCall(block: suspend (authorization: String) -> T): Result<T> =
        authorizedRequestExecutor.execute(block)

    private fun VehicleResponseDto.toDomain(): Vehicle =
        Vehicle(
            id = id,
            plate = plate,
            brand = brand,
            model = model,
            type = type.toVehicleType(),
            pricePerDay = pricePerDay,
            pricePerMinute = pricePerMinute,
            pricePerHour = pricePerHour,
            fuelPercent = fuelPercent,
            rangeKm = rangeKm,
            transmission = transmission.toVehicleTransmission(),
            seats = seats,
            segment = segment.toVehicleSegment(),
            status = status.toVehicleStatus(),
            latitude = latitude,
            longitude = longitude,
        )

    private fun String.toVehicleType(): VehicleType =
        when (uppercase()) {
            VehicleType.SEDAN.name -> VehicleType.SEDAN
            VehicleType.SUV.name -> VehicleType.SUV
            VehicleType.HATCHBACK.name -> VehicleType.HATCHBACK
            VehicleType.STATION.name -> VehicleType.STATION
            VehicleType.MINIVAN.name -> VehicleType.MINIVAN
            else -> VehicleType.SEDAN
        }

    private fun String.toVehicleSegment(): VehicleSegment =
        when (uppercase()) {
            VehicleSegment.ECONOMY.name -> VehicleSegment.ECONOMY
            VehicleSegment.COMFORT.name -> VehicleSegment.COMFORT
            VehicleSegment.SUV.name -> VehicleSegment.SUV
            else -> VehicleSegment.ECONOMY
        }

    private fun String.toVehicleTransmission(): VehicleTransmission =
        when (uppercase()) {
            VehicleTransmission.MANUAL.name -> VehicleTransmission.MANUAL
            VehicleTransmission.AUTOMATIC.name -> VehicleTransmission.AUTOMATIC
            else -> VehicleTransmission.AUTOMATIC
        }

    private fun String.toVehicleStatus(): VehicleStatus =
        when (uppercase()) {
            VehicleStatus.AVAILABLE.name -> VehicleStatus.AVAILABLE
            VehicleStatus.RESERVED.name -> VehicleStatus.RESERVED
            VehicleStatus.RENTED.name -> VehicleStatus.RENTED
            VehicleStatus.MAINTENANCE.name -> VehicleStatus.MAINTENANCE
            else -> VehicleStatus.AVAILABLE
        }

    private companion object {
        const val DEFAULT_PAGE_LIMIT = 100
    }
}
