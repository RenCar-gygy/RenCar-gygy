package com.turkcell.rencarapp.data.vehicle

import android.location.Location
import java.util.Locale

/**
 * Kullanıcı ve araç arasındaki mesafeyi hesaplar ve formatlar.
 */
object VehicleDistanceFormatter {

    /**
     * İki nokta arasındaki mesafeyi formatlı string olarak döner.
     */
    fun formatDistance(
        userLat: Double?,
        userLng: Double?,
        vehicleLat: Double?,
        vehicleLng: Double?
    ): String {
        if (userLat == null || userLng == null || vehicleLat == null || vehicleLng == null) {
            return "Mesafe hesaplanamadı"
        }

        val results = FloatArray(1)
        Location.distanceBetween(userLat, userLng, vehicleLat, vehicleLng, results)
        val distanceInMeters = results[0]

        return if (distanceInMeters < 1000) {
            "${distanceInMeters.toInt()} m uzaklıkta"
        } else {
            val distanceInKm = distanceInMeters / 1000f
            String.format(Locale.getDefault(), "%.1f km uzaklıkta", distanceInKm)
        }
    }
}
