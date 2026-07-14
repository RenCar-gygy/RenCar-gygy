package com.turkcell.rencarapp.data.reservation

import com.turkcell.rencarapp.data.auth.AuthorizedRequestExecutor
import com.turkcell.rencarapp.data.network.api.ReservationApi
import com.turkcell.rencarapp.data.network.dto.CreateReservationDto
import com.turkcell.rencarapp.data.network.dto.Reservation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultReservationRepository @Inject constructor(
    private val reservationApi: ReservationApi,
    private val authorizedRequestExecutor: AuthorizedRequestExecutor
) : ReservationRepository {

    override suspend fun create(vehicleId: String): Result<Reservation> =
        authorizedCall { token ->
            reservationApi.create(token, CreateReservationDto(vehicleId))
        }

    override suspend fun getActive(): Result<Reservation> =
        authorizedCall { token ->
            reservationApi.getActive(token)
        }

    override suspend fun cancel(id: String): Result<Unit> =
        authorizedCall { token ->
            reservationApi.cancel(token, id)
        }

    private suspend fun <T> authorizedCall(block: suspend (String) -> T): Result<T> =
        authorizedRequestExecutor.execute(block)
}
