package com.turkcell.rencarapp.data.rental

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Aktif kiralama ekranı ile start_photos arasındaki yerel kilidi-aç / foto tamamlama
 * durumunu NavBackStackEntry yaşam döngüsünden bağımsız tutar.
 */
@Singleton
class RentalUnlockSession @Inject constructor() {

    private val dailyStartPhotosCompleted = ConcurrentHashMap.newKeySet<String>()
    private val rideStarted = ConcurrentHashMap.newKeySet<String>()

    fun markDailyStartPhotosCompleted(rentalId: String) {
        if (rentalId.isBlank()) return
        dailyStartPhotosCompleted.add(rentalId)
    }

    fun isDailyStartPhotosCompleted(rentalId: String): Boolean =
        rentalId.isNotBlank() && rentalId in dailyStartPhotosCompleted

    fun markRideStarted(rentalId: String) {
        if (rentalId.isBlank()) return
        rideStarted.add(rentalId)
        dailyStartPhotosCompleted.add(rentalId)
    }

    fun isRideStarted(rentalId: String): Boolean =
        rentalId.isNotBlank() && rentalId in rideStarted

    fun clear(rentalId: String) {
        if (rentalId.isBlank()) return
        dailyStartPhotosCompleted.remove(rentalId)
        rideStarted.remove(rentalId)
    }
}
