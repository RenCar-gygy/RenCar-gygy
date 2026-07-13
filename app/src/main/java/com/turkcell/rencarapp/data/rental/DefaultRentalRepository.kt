package com.turkcell.rencarapp.data.rental

import com.turkcell.rencarapp.data.auth.AuthorizedRequestExecutor
import com.turkcell.rencarapp.data.network.api.RentalApi
import com.turkcell.rencarapp.data.network.dto.CreateRentalDto
import com.turkcell.rencarapp.data.network.dto.RentalResponseDto
import com.turkcell.rencarapp.data.network.dto.VehicleResponseDto
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultRentalRepository @Inject constructor(
    private val rentalApi: RentalApi,
    private val authorizedRequestExecutor: AuthorizedRequestExecutor,
) : RentalRepository {

    override suspend fun create(request: CreateRentalRequest): Result<Rental> =
        authorizedCall { authorization ->
            rentalApi.create(
                authorization = authorization,
                body = CreateRentalDto(
                    vehicleId = request.vehicleId,
                    endDate = request.endDate.toString()
                )
            ).toDomain()
        }

    override suspend fun listMine(): Result<List<Rental>> =
        authorizedCall { authorization ->
            rentalApi.listMine(authorization = authorization).map { it.toDomain() }
        }

    override suspend fun getById(id: String): Result<Rental> =
        authorizedCall { authorization ->
            rentalApi.getById(authorization = authorization, id = id).toDomain()
        }

    override suspend fun returnRental(id: String): Result<Rental> =
        authorizedCall { authorization ->
            rentalApi.returnRental(authorization = authorization, id = id).toDomain()
        }

    // YENİ EKLENEN KISIM: Araç detaylarını getiren metod
    override suspend fun getVehicleById(id: String): Result<VehicleResponseDto> =
        authorizedCall { authorization ->
            rentalApi.getVehicleById(authorization = authorization, id = id)
        }

    private suspend fun <T> authorizedCall(block: suspend (authorization: String) -> T): Result<T> =
        authorizedRequestExecutor.execute(block)

    private fun RentalResponseDto.toDomain(): Rental =
        Rental(
            id = id,
            userId = userId,
            vehicleId = vehicleId,
            startDate = Instant.parse(startDate),
            endDate = Instant.parse(endDate),
            totalPrice = totalPrice,
            status = status.toRentalStatus(),
        )

    private fun String.toRentalStatus(): RentalStatus =
        when (uppercase()) {
            RentalStatus.ACTIVE.name -> RentalStatus.ACTIVE
            RentalStatus.COMPLETED.name -> RentalStatus.COMPLETED
            RentalStatus.CANCELLED.name -> RentalStatus.CANCELLED
            RentalStatus.PREPARING.name -> RentalStatus.PREPARING // DTO'nda bu da vardı, eklemekte fayda var
            else -> RentalStatus.ACTIVE
        }
}