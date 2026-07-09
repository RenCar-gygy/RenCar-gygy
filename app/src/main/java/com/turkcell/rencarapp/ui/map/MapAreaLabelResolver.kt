package com.turkcell.rencarapp.ui.map

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class MapAreaLabelResolver @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    data class SearchAreaMatch(
        val latitude: Double,
        val longitude: Double,
        val areaName: String,
    )

    suspend fun resolveSearchArea(query: String): SearchAreaMatch? = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank() || !Geocoder.isPresent()) return@withContext null

        val geocoder = Geocoder(context, Locale.forLanguageTag("tr-TR"))
        val candidates = listOf(
            "$trimmed, Türkiye",
            "$trimmed, İstanbul, Türkiye",
            trimmed,
        )

        for (candidate in candidates) {
            val address = runCatching { forwardGeocode(geocoder, candidate) }.getOrNull()
                ?: continue

            return@withContext SearchAreaMatch(
                latitude = address.latitude,
                longitude = address.longitude,
                areaName = address.toSearchAreaName() ?: trimmed,
            )
        }

        null
    }

    suspend fun resolve(
        latitude: Double,
        longitude: Double,
        vehiclePins: List<MapVehiclePin>,
        preferredAreaName: String? = null,
    ): String = withContext(Dispatchers.IO) {
        val areaName = resolveAreaName(latitude, longitude, preferredAreaName)
        val nearestMinutes = nearestWalkingMinutes(
            userLatitude = latitude,
            userLongitude = longitude,
            vehiclePins = vehiclePins,
        )

        if (nearestMinutes != null) {
            "$areaName · $nearestMinutes dk uzaklıkta"
        } else {
            areaName
        }
    }

    private suspend fun resolveAreaName(
        latitude: Double,
        longitude: Double,
        preferredAreaName: String?,
    ): String {
        if (!preferredAreaName.isNullOrBlank()) {
            return "$preferredAreaName çevresinde"
        }

        if (!Geocoder.isPresent()) {
            return FALLBACK_AREA_LABEL
        }

        val address = runCatching { reverseGeocode(latitude, longitude) }.getOrNull()
        val districtName = address?.toDistrictName()

        return districtName?.let { "$it çevresinde" } ?: FALLBACK_AREA_LABEL
    }

    /**
     * Yalnızca ilçe düzeyinde isim döner (ör. Üsküdar, Kadıköy).
     * Mahalle (`subLocality`) ve sokak bilgisi kullanılmaz.
     */
    private fun Address.toDistrictName(): String? {
        subAdminArea
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }

        val province = adminArea?.takeIf { it.isNotBlank() }
        val localityName = locality?.takeIf { it.isNotBlank() }

        if (localityName != null && localityName != province) {
            return localityName
        }

        return province
    }

    /** Arama sonucu etiketi: mahalle + ilçe mümkünse birlikte döner. */
    private fun Address.toSearchAreaName(): String? {
        val district = toDistrictName()
        val neighborhood = subLocality?.takeIf { it.isNotBlank() }

        return when {
            neighborhood != null && district != null -> "$neighborhood, $district"
            neighborhood != null -> neighborhood
            district != null -> district
            else -> locality?.takeIf { it.isNotBlank() }
        }
    }

    private suspend fun forwardGeocode(geocoder: Geocoder, query: String): Address? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocationName(
                    query,
                    1,
                    object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (continuation.isActive) {
                                continuation.resume(addresses.firstOrNull())
                            }
                        }

                        override fun onError(errorMessage: String?) {
                            if (continuation.isActive) {
                                continuation.resume(null)
                            }
                        }
                    },
                )
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocationName(query, 1)?.firstOrNull()
        }
    }

    private suspend fun reverseGeocode(latitude: Double, longitude: Double): Address? {
        val geocoder = Geocoder(context, Locale.forLanguageTag("tr-TR"))

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(
                    latitude,
                    longitude,
                    1,
                    object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            if (continuation.isActive) {
                                continuation.resume(addresses.firstOrNull())
                            }
                        }

                        override fun onError(errorMessage: String?) {
                            if (continuation.isActive) {
                                continuation.resume(null)
                            }
                        }
                    },
                )
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
        }
    }

    private fun nearestWalkingMinutes(
        userLatitude: Double,
        userLongitude: Double,
        vehiclePins: List<MapVehiclePin>,
    ): Int? {
        val nearestMeters = vehiclePins
            .filterNot { it.isInUse }
            .map { pin ->
                haversineMeters(
                    userLatitude,
                    userLongitude,
                    pin.latitude,
                    pin.longitude,
                )
            }
            .minOrNull()
            ?: return null

        return (nearestMeters / WALKING_METERS_PER_MINUTE)
            .roundToInt()
            .coerceAtLeast(1)
    }

    private fun haversineMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
    ): Double {
        val earthRadiusMeters = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusMeters * c
    }

    companion object {
        private const val FALLBACK_AREA_LABEL = "Yakın çevrede"
        private const val WALKING_METERS_PER_MINUTE = 83.0
    }
}
