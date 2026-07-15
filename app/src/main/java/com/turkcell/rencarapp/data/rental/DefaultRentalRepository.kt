package com.turkcell.rencarapp.data.rental

import com.turkcell.rencarapp.data.auth.AuthorizedRequestExecutor
import com.turkcell.rencarapp.data.network.api.RentalApi
import com.turkcell.rencarapp.data.network.dto.ActiveRentalResponseDto
import com.turkcell.rencarapp.data.network.dto.CreateRentalDto
import com.turkcell.rencarapp.data.network.dto.RentalPhotosState
import com.turkcell.rencarapp.data.network.dto.RentalResponseDto
import com.turkcell.rencarapp.data.network.dto.VehicleResponseDto
import com.turkcell.rencarapp.data.network.toApiDateTimeString
import com.turkcell.rencarapp.data.network.dto.RentalPlan as NetworkRentalPlan
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultRentalRepository @Inject constructor(
    private val rentalApi: RentalApi,
    private val authorizedRequestExecutor: AuthorizedRequestExecutor,
    @ApplicationContext private val context: android.content.Context,
) : RentalRepository {

    override suspend fun create(request: CreateRentalRequest): Result<Rental> =
        authorizedCall { authorization ->
            rentalApi.create(
                authorization = authorization,
                body = CreateRentalDto(
                    vehicleId = request.vehicleId,
                    plan = request.plan.toNetwork(),
                    endDate = request.endDate?.toApiDateTimeString()
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

    override suspend fun getVehicleById(id: String): Result<VehicleResponseDto> =
        authorizedCall { authorization ->
            rentalApi.getVehicleById(authorization = authorization, id = id)
        }

    override suspend fun uploadPhoto(
        rentalId: String,
        side: String,
        imageUri: android.net.Uri
    ): Result<RentalPhotosState> {
        val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
            ?: return Result.failure(Exception("Dosya okunamadı."))
        return uploadPhotoBytes(rentalId, side, bytes)
    }

    override suspend fun uploadPhotoBytes(
        rentalId: String,
        side: String,
        bytes: ByteArray
    ): Result<RentalPhotosState> =
        authorizedCall { authorization ->
            rentalApi.uploadPhoto(
                authorization = authorization,
                id = rentalId,
                side = MultipartBody.Part.createFormData("side", side),
                file = MultipartBody.Part.createFormData(
                    "file",
                    "photo_${side}.jpg",
                    bytes.toRequestBody("image/jpeg".toMediaType())
                )
            )
        }

    override suspend fun getPhotos(rentalId: String): Result<RentalPhotosState> =
        authorizedCall { authorization ->
            rentalApi.getPhotos(authorization = authorization, id = rentalId)
        }

    override suspend fun start(rentalId: String): Result<Rental> =
        authorizedCall { authorization ->
            rentalApi.start(authorization = authorization, id = rentalId).toDomain()
        }

    override suspend fun getActive(): Result<ActiveRentalResponseDto> =
        authorizedCall { authorization ->
            rentalApi.getActive(authorization = authorization)
        }

    override suspend fun finish(rentalId: String): Result<Rental> =
        authorizedCall { authorization ->
            rentalApi.finish(authorization = authorization, id = rentalId).toDomain()
        }

    override suspend fun cancel(rentalId: String): Result<Unit> =
        authorizedCall { authorization ->
            rentalApi.cancel(authorization = authorization, id = rentalId)
        }

    private suspend fun <T> authorizedCall(block: suspend (authorization: String) -> T): Result<T> =
        authorizedRequestExecutor.execute(block)

    private fun RentalResponseDto.toDomain(): Rental =
        Rental(
            id = id,
            userId = userId,
            vehicleId = vehicleId,
            plan = plan.toDomainPlan(),
            startDate = Instant.parse(startedAt),
            endDate = endedAt?.let { Instant.parse(it) } ?: endDate?.let { Instant.parse(it) },
            totalPrice = totalPrice ?: 0.0,
            startFee = startFee,
            serviceFee = serviceFee ?: 0.0,
            distanceKm = distanceKm,
            durationMinutes = durationMinutes,
            status = status.toRentalStatus(),
            paymentStatus = paymentStatus,
            vehicleBrand = vehicle.brand,
            vehicleModel = vehicle.model,
            vehiclePlate = vehicle.plate,
        )

    private fun NetworkRentalPlan.toDomainPlan(): RentalPlan =
        when (this) {
            NetworkRentalPlan.PER_MINUTE -> RentalPlan.PER_MINUTE
            NetworkRentalPlan.HOURLY -> RentalPlan.HOURLY
            NetworkRentalPlan.DAILY -> RentalPlan.DAILY
        }

    private fun String.toRentalStatus(): RentalStatus =
        when (uppercase()) {
            RentalStatus.ACTIVE.name -> RentalStatus.ACTIVE
            RentalStatus.COMPLETED.name -> RentalStatus.COMPLETED
            RentalStatus.CANCELLED.name -> RentalStatus.CANCELLED
            RentalStatus.PREPARING.name -> RentalStatus.PREPARING
            else -> RentalStatus.ACTIVE
        }
}
