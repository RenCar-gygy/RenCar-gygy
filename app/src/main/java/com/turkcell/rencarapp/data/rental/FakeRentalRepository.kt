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
    private val uploadedSides = mutableMapOf<String, MutableSet<String>>()

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
        val rental = fakeRental(
            id = "rent-${rentals.size + 1}",
            vehicleId = request.vehicleId,
            plan = request.plan,
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

    override suspend fun getVehicleById(id: String): Result<VehicleResponseDto> {
        delay(FAKE_DELAY_MS)
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
    ): Result<com.turkcell.rencarapp.data.network.dto.RentalPhotosState> =
        uploadPhotoBytes(rentalId, side, RentalPhotoStub.JPEG_BYTES)

    override suspend fun uploadPhotoBytes(
        rentalId: String,
        side: String,
        bytes: ByteArray
    ): Result<com.turkcell.rencarapp.data.network.dto.RentalPhotosState> {
        delay(FAKE_DELAY_MS)
        uploadedSides.getOrPut(rentalId) { mutableSetOf() }.add(side)
        val uploaded = uploadedSides[rentalId].orEmpty()
        val remaining = RentalPhotoStub.REQUIRED_SIDES.filter { it !in uploaded }
        return Result.success(
            com.turkcell.rencarapp.data.network.dto.RentalPhotosState(
                rentalId = rentalId,
                photos = emptyList(),
                uploadedCount = uploaded.size,
                remainingSides = remaining,
                photosComplete = remaining.isEmpty()
            )
        )
    }

    override suspend fun getPhotos(rentalId: String): Result<com.turkcell.rencarapp.data.network.dto.RentalPhotosState> {
        delay(FAKE_DELAY_MS)
        val uploaded = uploadedSides[rentalId].orEmpty()
        val remaining = RentalPhotoStub.REQUIRED_SIDES.filter { it !in uploaded }
        return Result.success(
            com.turkcell.rencarapp.data.network.dto.RentalPhotosState(
                rentalId = rentalId,
                photos = emptyList(),
                uploadedCount = uploaded.size,
                remainingSides = remaining,
                photosComplete = remaining.isEmpty()
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
                    plate = active.vehiclePlate,
                    brand = active.vehicleBrand,
                    model = active.vehicleModel,
                    type = "SEDAN"
                ),
                plan = active.plan.toNetwork(),
                startedAt = active.startDate.toString(),
                elapsedSeconds = 1200,
                currentCost = 45.0,
                distanceKm = active.distanceKm,
                status = "ACTIVE",
                paymentStatus = active.paymentStatus,
                discountAmount = 0.0,
                createdAt = active.startDate.toString(),
                startFee = active.startFee,
                durationMinutes = active.durationMinutes
            )
        )
    }

    override suspend fun finish(rentalId: String): Result<Rental> {
        delay(FAKE_DELAY_MS)
        val index = rentals.indexOfFirst { it.id == rentalId }
        if (index < 0) return Result.failure(NoSuchElementException("Kiralama bulunamadı."))
        val current = rentals[index]
        if (current.status != RentalStatus.ACTIVE) {
            return Result.failure(IllegalStateException("Yalnızca aktif kiralamalar sonlandırılabilir."))
        }
        val completed = current.copy(
            status = RentalStatus.COMPLETED,
            endDate = Instant.now(),
        )
        rentals[index] = completed
        return Result.success(completed)
    }

    override suspend fun cancel(rentalId: String): Result<Unit> {
        delay(FAKE_DELAY_MS)
        val index = rentals.indexOfFirst { it.id == rentalId }
        if (index < 0) return Result.failure(NoSuchElementException("Kiralama bulunamadı."))
        val current = rentals[index]
        if (current.status != RentalStatus.PREPARING && current.status != RentalStatus.ACTIVE) {
            return Result.failure(IllegalStateException("Bu kiralama iptal edilemez."))
        }
        rentals[index] = current.copy(status = RentalStatus.CANCELLED)
        return Result.success(Unit)
    }

    private fun fakeRental(
        id: String,
        vehicleId: String,
        plan: RentalPlan,
        startDate: Instant,
        endDate: Instant?,
        totalPrice: Double,
        status: RentalStatus,
    ): Rental =
        Rental(
            id = id,
            userId = "fake-user",
            vehicleId = vehicleId,
            plan = plan,
            startDate = startDate,
            endDate = endDate,
            totalPrice = totalPrice,
            startFee = 15.0,
            serviceFee = totalPrice * 0.10,
            distanceKm = 5.2,
            durationMinutes = 20,
            status = status,
            paymentStatus = "UNPAID",
            vehicleBrand = "Volkswagen",
            vehicleModel = "Passat",
            vehiclePlate = "34 ABC 123",
        )

    private companion object {
        const val FAKE_DELAY_MS = 400L
    }
}
