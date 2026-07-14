package com.turkcell.rencarapp.data.rental

import com.turkcell.rencarapp.data.auth.AuthorizedRequestExecutor
import com.turkcell.rencarapp.data.network.api.RentalApi
import com.turkcell.rencarapp.data.network.dto.ActiveRentalResponseDto
import com.turkcell.rencarapp.data.network.dto.CreateRentalDto
import com.turkcell.rencarapp.data.network.dto.RentalPhotosState
import com.turkcell.rencarapp.data.network.dto.RentalResponseDto
import com.turkcell.rencarapp.data.network.dto.VehicleResponseDto
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
                    plan = when (request.plan) {
                        RentalPlan.PER_MINUTE -> com.turkcell.rencarapp.data.network.dto.RentalPlan.PER_MINUTE
                        RentalPlan.HOURLY -> com.turkcell.rencarapp.data.network.dto.RentalPlan.HOURLY
                        RentalPlan.DAILY -> com.turkcell.rencarapp.data.network.dto.RentalPlan.DAILY
                    },
                    endDate = request.endDate?.toString()
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

    override suspend fun uploadPhoto(
        rentalId: String,
        side: String,
        imageUri: android.net.Uri
    ): Result<RentalPhotosState> {
        val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
            ?: return Result.failure(Exception("Dosya okunamadı."))

        return authorizedCall { authorization ->
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

    private suspend fun <T> authorizedCall(block: suspend (authorization: String) -> T): Result<T> =
        authorizedRequestExecutor.execute(block)

    private fun RentalResponseDto.toDomain(): Rental =
        Rental(
            id = id,
            userId = userId,
            vehicleId = vehicleId,
            startDate = Instant.parse(startedAt),
            endDate = endDate?.let { Instant.parse(it) } ?: Instant.now(),
            totalPrice = totalPrice ?: 0.0,
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