package com.turkcell.rencarapp.data.vehicle

import com.turkcell.rencarapp.data.network.api.VehicleApi
import com.turkcell.rencarapp.data.network.dto.VehicleResponseDto
import com.turkcell.rencarapp.data.session.SessionStore
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultVehicleRepository @Inject constructor(
    private val vehicleApi: VehicleApi,
    private val sessionStore: SessionStore,
) : VehicleRepository {

    override suspend fun listAvailable(type: VehicleType?): Result<List<Vehicle>> =
        authorizedCall { authorization ->
            vehicleApi.listAvailable(
                authorization = authorization,
                type = type?.name,
                limit = DEFAULT_PAGE_LIMIT,
            ).map { it.toDomain() }
        }

    override suspend fun getById(id: String): Result<Vehicle> =
        authorizedCall { authorization ->
            vehicleApi.getById(authorization = authorization, id = id).toDomain()
        }

    private suspend fun <T> authorizedCall(block: suspend (authorization: String) -> T): Result<T> {
        val session = sessionStore.getSession()
            ?: return Result.failure(IllegalStateException("Oturum bulunamadı."))
        return apiCall {
            block(bearer(session.accessToken))
        }
    }

    private suspend fun <T> apiCall(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (exception: HttpException) {
            Result.failure(IllegalStateException(httpErrorMessage(exception)))
        } catch (exception: Exception) {
            Result.failure(exception)
        }

    private fun VehicleResponseDto.toDomain(): Vehicle =
        Vehicle(
            id = id,
            plate = plate,
            brand = brand,
            model = model,
            type = type.toVehicleType(),
            pricePerDay = pricePerDay,
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

    private fun String.toVehicleStatus(): VehicleStatus =
        when (uppercase()) {
            VehicleStatus.AVAILABLE.name -> VehicleStatus.AVAILABLE
            VehicleStatus.RENTED.name -> VehicleStatus.RENTED
            VehicleStatus.MAINTENANCE.name -> VehicleStatus.MAINTENANCE
            else -> VehicleStatus.AVAILABLE
        }

    private fun bearer(accessToken: String): String = "Bearer $accessToken"

    private fun httpErrorMessage(exception: HttpException): String =
        when (exception.code()) {
            401 -> "Kimlik doğrulama başarısız."
            403 -> "Araç listesine erişmek için ehliyet onayı gerekir."
            404 -> "Araç bulunamadı veya müsait değil."
            else -> "Sunucu hatası (${exception.code()})."
        }

    private companion object {
        const val DEFAULT_PAGE_LIMIT = 100
    }
}
