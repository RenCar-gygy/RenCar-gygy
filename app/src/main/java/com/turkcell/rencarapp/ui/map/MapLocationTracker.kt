package com.turkcell.rencarapp.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.turkcell.rencarapp.BuildConfig
import org.maplibre.android.geometry.LatLng
import kotlin.math.abs

/** Emülatör testleri için Üsküdar Meydan referans noktası. */
private val USKUDAR_TEST_LOCATION = LatLng(41.0257, 29.0151)

data class MapUserLocationState(
    val location: LatLng? = null,
    val hasPermission: Boolean = false,
    val requestPermission: () -> Unit = {},
    val refreshAndGetLocation: (onResult: (LatLng?) -> Unit) -> Unit = { it(null) },
)

@Composable
fun rememberMapUserLocation(requestOnLaunch: Boolean = true): MapUserLocationState {
    val context = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var location by remember { mutableStateOf<LatLng?>(null) }
    var hasPermission by remember {
        mutableStateOf(hasLocationPermission(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasPermission = granted
    }

    val requestPermission: () -> Unit = {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
        )
    }

    val refreshAndGetLocation: (onResult: (LatLng?) -> Unit) -> Unit = { onResult ->
        if (hasPermission) {
            fetchCurrentLocation(context, fusedClient) { resolved ->
                location = resolved
                onResult(resolved)
            }
        } else {
            onResult(null)
        }
    }

    LaunchedEffect(hasPermission) {
        if (!hasPermission && requestOnLaunch) {
            requestPermission()
        }
    }

    DisposableEffect(hasPermission) {
        if (!hasPermission) {
            location = null
            return@DisposableEffect onDispose { }
        }

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    location = normalizeLocation(context, loc.latitude, loc.longitude)
                }
            }
        }

        startLocationUpdates(context, fusedClient, callback)

        onDispose {
            fusedClient.removeLocationUpdates(callback)
        }
    }

    return MapUserLocationState(
        location = location,
        hasPermission = hasPermission,
        requestPermission = requestPermission,
        refreshAndGetLocation = refreshAndGetLocation,
    )
}

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

@SuppressLint("MissingPermission")
private fun startLocationUpdates(
    context: Context,
    fusedClient: FusedLocationProviderClient,
    callback: LocationCallback,
) {
    val request = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        5_000L,
    ).setMinUpdateIntervalMillis(2_000L).build()

    fusedClient.lastLocation.addOnSuccessListener { lastLocation ->
        if (lastLocation != null) {
            callback.onLocationResult(LocationResult.create(listOf(lastLocation)))
        }
    }

    fusedClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
}

@SuppressLint("MissingPermission")
private fun fetchCurrentLocation(
    context: Context,
    fusedClient: FusedLocationProviderClient,
    onResult: (LatLng?) -> Unit,
) {
    val cancellationToken = CancellationTokenSource().token
    fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationToken)
        .addOnSuccessListener { lastLocation ->
            onResult(
                lastLocation?.let { loc ->
                    normalizeLocation(context, loc.latitude, loc.longitude)
                },
            )
        }
        .addOnFailureListener {
            onResult(null)
        }
}

/**
 * Emülatör, Google Play Services önbelleğinden Mountain View döndürdüğünde
 * debug build'de Üsküdar test koordinatına düşer.
 */
private fun normalizeLocation(context: Context, latitude: Double, longitude: Double): LatLng {
    val raw = LatLng(latitude, longitude)
    if (!BuildConfig.DEBUG || !isRunningOnEmulator()) return raw
    return if (isLikelyDefaultEmulatorLocation(latitude, longitude)) {
        USKUDAR_TEST_LOCATION
    } else {
        raw
    }
}

private fun isRunningOnEmulator(): Boolean =
    Build.FINGERPRINT.startsWith("generic") ||
        Build.FINGERPRINT.contains("emulator", ignoreCase = true) ||
        Build.HARDWARE.contains("goldfish", ignoreCase = true) ||
        Build.HARDWARE.contains("ranchu", ignoreCase = true) ||
        Build.PRODUCT.contains("sdk", ignoreCase = true)

private fun isLikelyDefaultEmulatorLocation(latitude: Double, longitude: Double): Boolean =
    abs(latitude - 37.422) < 2.0 && abs(longitude + 122.084) < 2.0
