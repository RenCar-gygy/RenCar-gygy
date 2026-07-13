package com.turkcell.rencarapp.data.rental

import com.turkcell.rencarapp.data.network.dto.VehicleResponseDto
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeRentalRepository @Inject constructor() : RentalRepository {

    private val rentals = mutableListOf<Rental>()

    override suspend fun create(request: CreateRentalRequest): Result<Rental> {
        delay(FAKE_DELAY_MS)
        if (request.endDate.isBefore(Instant.now())) {
            return Result.failure(IllegalArgumentException("Bitiş tarihi geçmişte olamaz."))
        }
        if (rentals.any { it.status == RentalStatus.ACTIVE }) {
            return Result.failure(IllegalStateException("Zaten aktif bir kiralamanız var."))
        }
        val days = ChronoUnit.DAYS.between(Instant.now(), request.endDate).coerceAtLeast(1)
        val rental = Rental(
            id = "rent-${rentals.size + 1}",
            userId = "fake-user",
            vehicleId = request.vehicleId,
            startDate = Instant.now(),
            endDate = request.endDate,
            totalPrice = days * 1500.0,
            status = RentalStatus.ACTIVE,
        )
        rentals += rental
        return Result.success(rental)
    }

    override suspend fun listMine(): Result<List<Rental>> {
        delay(FAKE_DELAY_MS)
        return Result.success(rentals.sortedByDescending { it.startDate })
    }

    override suspend fun getById(id: String): Result<Rental> {
        delay(FAKE_DELAY_MS)
        return rentals.find { it.id == id }?.let { Result.success(it) }
            ?: Result.failure(NoSuchElementException("Kiralama bulunamadı."))
    }

    override suspend fun returnRental(id: String): Result<Rental> {
        delay(FAKE_DELAY_MS)
        val index = rentals.indexOfFirst { it.id == id }
        if (index < 0) return Result.failure(NoSuchElementException("Kiralama bulunamadı."))
        val current = rentals[index]
        if (current.status != RentalStatus.ACTIVE) {
            return Result.failure(IllegalStateException("Yalnızca aktif kiralamalar iade edilebilir."))
        }
        val completed = current.copy(status = RentalStatus.COMPLETED)
        rentals[index] = completed
        return Result.success(completed)
    }

    // YENİ EKLENEN KISIM: Fake araç verisi döndüren metod
    override suspend fun getVehicleById(id: String): Result<VehicleResponseDto> {
        delay(FAKE_DELAY_MS)
        // Fake olduğu için ekranda güzel duracak sabit bir araç modeli dönüyoruz
        val fakeVehicle = VehicleResponseDto(
            id = id,
            plate = "34 TRK 99",
            brand = "Renault",
            model = "Clio",
            type = "HATCHBACK",
            pricePerDay = 1500.0,
            status = "RENTED",
            latitude = 41.02,
            longitude = 28.98
        )
        return Result.success(fakeVehicle)
    }

    private companion object {
        const val FAKE_DELAY_MS = 400L
    }
}