package com.turkcell.rencarapp.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import okhttp3.OkHttpClient
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.android.module.http.HttpRequestUtil
import kotlin.math.roundToInt

private const val DEFAULT_ZOOM = 12.0
private val DEFAULT_CENTER = LatLng(41.0151, 28.9795)
private val EconomicOrange = Color(0xFFF97316)
private val ComfortPurple = Color(0xFFA855F7)
private val SuvYellow = Color(0xFFEAB308)
private val TealPin = Color(0xFF14B8A6)

private val OSM_STYLE_JSON = """
{
  "version": 8,
  "sources": {
    "osm": {
      "type": "raster",
      "tiles": ["https://tile.openstreetmap.org/{z}/{x}/{y}.png"],
      "tileSize": 256,
      "attribution": "(c) OpenStreetMap contributors"
    }
  },
  "layers": [
    {
      "id": "osm",
      "type": "raster",
      "source": "osm"
    }
  ]
}
""".trimIndent()

private var httpClientConfigured = false

private data class PinScreenPosition(
    val pin: MapVehiclePin,
    val xPx: Float,
    val yPx: Float,
)

@Composable
fun MapLibreMapView(
    pins: List<MapVehiclePin>,
    onPinClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestPins by rememberUpdatedState(pins)
    val latestOnPinClick by rememberUpdatedState(onPinClick)
    var mapRef by remember { mutableStateOf<MapLibreMap?>(null) }
    var pinPositions by remember { mutableStateOf<List<PinScreenPosition>>(emptyList()) }

    remember {
        MapLibre.getInstance(context.applicationContext)
        if (!httpClientConfigured) {
            HttpRequestUtil.setOkHttpClient(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .header("User-Agent", "RenCarApp/1.0 (Android; contact@rencar.local)")
                            .build()
                        chain.proceed(request)
                    }
                    .build(),
            )
            httpClientConfigured = true
        }
    }

    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
        }
    }

    fun refreshPinPositions(map: MapLibreMap, currentPins: List<MapVehiclePin>) {
        pinPositions = currentPins.mapNotNull { pin ->
            val screen = map.projection.toScreenLocation(LatLng(pin.latitude, pin.longitude))
            PinScreenPosition(pin = pin, xPx = screen.x, yPx = screen.y)
        }
    }

    fun bindMap(map: MapLibreMap) {
        mapRef = map
        map.setStyle(Style.Builder().fromJson(OSM_STYLE_JSON)) {
            moveCameraToPins(map, latestPins)
            refreshPinPositions(map, latestPins)
            map.addOnCameraIdleListener {
                refreshPinPositions(map, latestPins)
            }
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        var mapReadyCallback: OnMapReadyCallback? = null
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    mapView.onStart()
                    if (mapRef == null) {
                        mapReadyCallback = OnMapReadyCallback { map -> bindMap(map) }
                        mapView.getMapAsync(mapReadyCallback!!)
                    }
                }
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapRef = null
            pinPositions = emptyList()
            mapView.onDestroy()
        }
    }

    DisposableEffect(pins, mapRef) {
        val map = mapRef
        if (map != null && map.style != null) {
            moveCameraToPins(map, pins)
            refreshPinPositions(map, pins)
        }
        onDispose { }
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
        )

        pinPositions.forEach { position ->
            MapPinOverlay(
                pin = position.pin,
                modifier = Modifier.offset {
                    IntOffset(
                        (position.xPx - 24.dp.toPx()).roundToInt(),
                        (position.yPx - 40.dp.toPx()).roundToInt(),
                    )
                },
                onClick = { latestOnPinClick(position.pin.id) },
            )
        }
    }
}

@Composable
private fun MapPinOverlay(
    pin: MapVehiclePin,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val inUseColor = if (isSystemInDarkTheme()) Color(0xFF64748B) else Color(0xFF94A3B8)
    val pinColor = when {
        pin.isInUse -> inUseColor
        pin.category == VehicleCategory.ECONOMIC -> EconomicOrange
        pin.category == VehicleCategory.COMFORT -> ComfortPurple
        pin.category == VehicleCategory.SUV -> SuvYellow
        else -> TealPin
    }

    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier
                .shadow(6.dp, RoundedCornerShape(20.dp))
                .clip(RoundedCornerShape(20.dp))
                .background(pinColor)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!pin.isInUse) {
                Icon(
                    imageVector = Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp),
                )
            }
            Text(
                text = pin.priceLabel,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = if (pin.isInUse) 0.dp else 4.dp),
            )
        }
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
                .background(pinColor),
        )
    }
}

private fun moveCameraToPins(map: MapLibreMap, pins: List<MapVehiclePin>) {
    if (pins.isEmpty()) {
        map.cameraPosition = CameraPosition.Builder()
            .target(DEFAULT_CENTER)
            .zoom(DEFAULT_ZOOM)
            .build()
        return
    }

    if (pins.size == 1) {
        val pin = pins.first()
        map.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .target(LatLng(pin.latitude, pin.longitude))
                    .zoom(14.0)
                    .build(),
            ),
        )
        return
    }

    val boundsBuilder = LatLngBounds.Builder()
    pins.forEach { pin ->
        boundsBuilder.include(LatLng(pin.latitude, pin.longitude))
    }
    try {
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120))
    } catch (_: Exception) {
        val first = pins.first()
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(first.latitude, first.longitude), 14.0),
        )
    }
}
