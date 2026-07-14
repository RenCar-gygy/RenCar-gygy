package com.turkcell.rencarapp.data.reservation

import com.turkcell.rencarapp.data.network.dto.Reservation

interface ReservationRepository {
    suspend fun create(vehicleId: String): Result<Reservation>
    suspend fun getActive(): Result<Reservation>
    suspend fun cancel(id: String): Result<Unit>
}
