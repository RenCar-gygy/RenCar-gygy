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
        val endDate = request.endDate ?: Instant.now().plus(1, ChronoUnit.HOURS)
        if (endDate.isBefore(Instant.now())) {
            return Result.failure(IllegalArgumentException("Bitiş tarihi geçmişte olamaz."))
        }
        if (rentals.any { it.status == RentalStatus.ACTIVE }) {
            return Result.failure(IllegalStateException("Zaten aktif bir kiralamanız var."))
        }
        val days = ChronoUnit.DAYS.between(Instant.now(), endDate).coerceAtLeast(1)
        val rental = Rental(
            id = "rent-${rentals.size + 1}",
            userId = "fake-user",
            vehicleId = request.vehicleId,
            startDate = Instant.now(),
            endDate = endDate,
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
            longitude = 28.98,
            pricePerMinute = 0.67,
            pricePerHour = 40.42,
            fuelPercent = 72,
            rangeKm = 480,
            transmission = "Manuel",
            seats = 5,
            segment = "HATCHBACK"
        )
        return Result.success(fakeVehicle)
    }

    override suspend fun uploadPhoto(
        rentalId: String,
        side: String,
        imageUri: android.net.Uri
    ): Result<com.turkcell.rencarapp.data.network.dto.RentalPhotosState> {
        delay(FAKE_DELAY_MS)
        return Result.success(
            com.turkcell.rencarapp.data.network.dto.RentalPhotosState(
                rentalId = rentalId,
                photos = emptyList(),
                uploadedCount = 1,
                remainingSides = listOf("BACK", "LEFT", "RIGHT"),
                photosComplete = false
            )
        )
    }

    override suspend fun getPhotos(rentalId: String): Result<com.turkcell.rencarapp.data.network.dto.RentalPhotosState> {
        delay(FAKE_DELAY_MS)
        return Result.success(
            com.turkcell.rencarapp.data.network.dto.RentalPhotosState(
                rentalId = rentalId,
                photos = emptyList(),
                uploadedCount = 0,
                remainingSides = listOf("FRONT", "BACK", "LEFT", "RIGHT"),
                photosComplete = false
            )
        )
    }

    override suspend fun start(rentalId: String): Result<Rental> {
        delay(FAKE_DELAY_MS)
        val index = rentals.indexOfFirst { it.id == rentalId }
        if (index < 0) return Result.failure(NoSuchElementException("Kiralama bulunamadı."))
        val updated = rentals[index].copy(status = RentalStatus.ACTIVE)
        rentals[index] = updated
        return Result.success(updated)
    }

    override suspend fun getActive(): Result<com.turkcell.rencarapp.data.network.dto.ActiveRentalResponseDto> {
        delay(FAKE_DELAY_MS)
        val active = rentals.find { it.status == RentalStatus.ACTIVE }
            ?: return Result.failure(NoSuchElementException("Aktif kiralama yok."))
            
        return Result.success(
            com.turkcell.rencarapp.data.network.dto.ActiveRentalResponseDto(
                id = active.id,
                userId = active.userId,
                vehicleId = active.vehicleId,
                vehicle = com.turkcell.rencarapp.data.network.dto.RentalVehicleSummaryDto(
                    id = active.vehicleId,
                    plate = "34 ABC 123",
                    brand = "Volkswagen",
                    model = "Passat",
                    type = "SEDAN"
                ),
                plan = com.turkcell.rencarapp.data.network.dto.RentalPlan.PER_MINUTE,
                startedAt = active.startDate.toString(),
                elapsedSeconds = 1200,
                currentCost = 45.0,
                distanceKm = 5.2,
                status = "ACTIVE",
                paymentStatus = "UNPAID",
                discountAmount = 0.0,
                createdAt = active.startDate.toString(),
                startFee = 15.0,
                durationMinutes = 20
            )
        )
    }

    override suspend fun finish(rentalId: String): Result<Rental> = returnRental(rentalId)

    private companion object {
        const val FAKE_DELAY_MS = 400L
    }
}