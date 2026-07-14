package com.turkcell.rencarapp.data.reservation

import com.turkcell.rencarapp.data.network.dto.Reservation
import com.turkcell.rencarapp.data.network.dto.ReservationVehicleSummaryDto
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeReservationRepository @Inject constructor() : ReservationRepository {

    private var activeReservation: Reservation? = null

    override suspend fun create(vehicleId: String): Result<Reservation> {
        delay(500)
        if (activeReservation != null) {
            return Result.failure(IllegalStateException("Zaten aktif bir rezervasyonunuz var."))
        }
        val reservation = Reservation(
            id = "res-${System.currentTimeMillis()}",
            userId = "user-123",
            vehicleId = vehicleId,
            vehicle = ReservationVehicleSummaryDto(
                id = vehicleId,
                plate = "34 ABC 123",
                brand = "Renault",
                model = "Clio",
                type = "HATCHBACK",
                latitude = 41.01,
                longitude = 28.97,
                pricePerMinute = 4.5
            ),
            status = "ACTIVE",
            expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES).toString(),
            remainingSeconds = 900,
            createdAt = Instant.now().toString()
        )
        activeReservation = reservation
        return Result.success(reservation)
    }

    override suspend fun getActive(): Result<Reservation> {
        delay(300)
        return activeReservation?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("Aktif rezervasyon yok."))
    }

    override suspend fun cancel(id: String): Result<Unit> {
        delay(300)
        if (activeReservation?.id == id) {
            activeReservation = null
            return Result.success(Unit)
        }
        return Result.failure(NoSuchElementException("Rezervasyon bulunamadı."))
    }
}
