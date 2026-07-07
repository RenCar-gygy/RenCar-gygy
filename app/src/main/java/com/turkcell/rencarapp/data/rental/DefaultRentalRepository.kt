package com.turkcell.rencarapp.data.rental

import com.turkcell.rencarapp.data.network.api.RentalApi
import com.turkcell.rencarapp.data.network.dto.CreateRentalDto
import com.turkcell.rencarapp.data.network.dto.RentalResponseDto
import com.turkcell.rencarapp.data.session.SessionStore
import retrofit2.HttpException
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultRentalRepository @Inject constructor(
    private val rentalApi: RentalApi,
    private val sessionStore: SessionStore,
) : RentalRepository {

    override suspend fun create(request: CreateRentalRequest): Result<Rental> =
        authorizedCall { authorization ->
            rentalApi.create(
                authorization = authorization,
                body = CreateRentalDto(
                    vehicleId = request.vehicleId,
                    endDate = request.endDate.toString() // API formatına göre düzenlenebilir
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
            else -> RentalStatus.ACTIVE
        }

    private fun bearer(accessToken: String): String = "Bearer $accessToken"

    private fun httpErrorMessage(exception: HttpException): String =
        when (exception.code()) {
            401 -> "Kimlik doğrulama başarısız."
            403 -> "Bu işlem için yetkiniz yok."
            404 -> "Kiralama bulunamadı."
            409 -> "Zaten aktif bir kiralamanın mevcut. Önce onu bitirmelisin."
            else -> "Sunucu hatası (${exception.code()})."
        }
}
