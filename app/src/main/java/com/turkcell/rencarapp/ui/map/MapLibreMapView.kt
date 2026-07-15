package com.turkcell.rencarapp.ui.map

import android.graphics.Color as AndroidColor
import android.util.Log
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

private const val TAG = "MapLibreMapView"
private const val DEFAULT_ZOOM = 12.0
private const val USER_LOCATION_ZOOM = 14.0
private val DEFAULT_CENTER = LatLng(41.0151, 28.9795)
private val EconomicOrange = Color(0xFFF97316)
private val ComfortPurple = Color(0xFFA855F7)
private val SuvYellow = Color(0xFFEAB308)
private val TealPin = Color(0xFF14B8A6)

private var httpClientConfigured = false

private data class PinScreenPosition(
    val pin: MapVehiclePin,
    val xPx: Float,
    val yPx: Float,
)

private sealed interface MapPinDisplayItem {
    data class Single(
        val pin: MapVehiclePin,
        val xPx: Float,
        val yPx: Float,
    ) : MapPinDisplayItem

    data class Cluster(
        val pins: List<MapVehiclePin>,
        val xPx: Float,
        val yPx: Float,
        val count: Int,
    ) : MapPinDisplayItem {
        val centerLat: Double get() = pins.map { it.latitude }.average()
        val centerLng: Double get() = pins.map { it.longitude }.average()
    }
}

class MapCameraActions {
    internal var map: MapLibreMap? = null

    fun zoomIn() {
        map?.animateCamera(CameraUpdateFactory.zoomBy(ZOOM_STEP))
    }

    fun zoomOut() {
        val map = map ?: return
        val currentZoom = map.cameraPosition.zoom
        if (currentZoom > map.minZoomLevel) {
            map.animateCamera(CameraUpdateFactory.zoomBy(-ZOOM_STEP))
        }
    }
}

@Composable
fun MapLibreMapView(
    pins: List<MapVehiclePin>,
    onPinClick: (String) -> Unit,
    cameraActions: MapCameraActions = remember { MapCameraActions() },
    myLocation: LatLng? = null,
    focusMyLocation: Boolean = false,
    onMyLocationFocused: () -> Unit = {},
    focusVisiblePins: Boolean = false,
    onVisiblePinsFocused: () -> Unit = {},
    searchFocusLocation: LatLng? = null,
    focusSearchArea: Boolean = false,
    onSearchAreaFocused: () -> Unit = {},
    gesturesEnabled: Boolean = true,
    followLocationWithPan: Boolean = false,
    pinFocusZoom: Double = PIN_FOCUS_ZOOM,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestPins by rememberUpdatedState(pins)
    val latestOnPinClick by rememberUpdatedState(onPinClick)
    val latestMyLocation by rememberUpdatedState(myLocation)
    val latestFocusMyLocation by rememberUpdatedState(focusMyLocation)
    val latestOnMyLocationFocused by rememberUpdatedState(onMyLocationFocused)
    val latestFocusVisiblePins by rememberUpdatedState(focusVisiblePins)
    val latestOnVisiblePinsFocused by rememberUpdatedState(onVisiblePinsFocused)
    val latestSearchFocusLocation by rememberUpdatedState(searchFocusLocation)
    val latestFocusSearchArea by rememberUpdatedState(focusSearchArea)
    val latestOnSearchAreaFocused by rememberUpdatedState(onSearchAreaFocused)
    val latestPinFocusZoom by rememberUpdatedState(pinFocusZoom)
    val latestFollowLocationWithPan by rememberUpdatedState(followLocationWithPan)
    val latestCameraActions by rememberUpdatedState(cameraActions)
    val density = LocalDensity.current
    val clusterRadiusPx = remember(density) { with(density) { CLUSTER_RADIUS_DP.dp.toPx() } }
    var mapRef by remember { mutableStateOf<MapLibreMap?>(null) }
    var mapStyle by remember { mutableStateOf<Style?>(null) }
    var displayItems by remember { mutableStateOf<List<MapPinDisplayItem>>(emptyList()) }
    var forceExpandedPinIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var hasPerformedInitialZoom by remember { mutableStateOf(false) }
    var previousFollowLocation by remember { mutableStateOf<LatLng?>(null) }

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
            isFocusable = false
            isFocusableInTouchMode = false
        }
    }

    fun refreshDisplayItems(map: MapLibreMap, currentPins: List<MapVehiclePin>) {
        val zoom = map.cameraPosition.zoom
        if (zoom < CLUSTER_RECLUSTER_ZOOM) {
            forceExpandedPinIds = emptySet()
        }

        val screenPositions = currentPins.map { pin ->
            val screen = map.projection.toScreenLocation(LatLng(pin.latitude, pin.longitude))
            PinScreenPosition(pin = pin, xPx = screen.x, yPx = screen.y)
        }
        displayItems = applySpiderSpread(
            items = clusterPins(
                positions = screenPositions,
                baseRadiusPx = clusterRadiusPx,
                zoom = zoom,
                forceExpandedPinIds = forceExpandedPinIds,
            ),
            separationPx = clusterRadiusPx * PIN_SPREAD_SEPARATION_FACTOR,
            zoom = zoom,
        )
    }

    fun bindMap(map: MapLibreMap) {
        mapRef = map
        latestCameraActions.map = map
        map.uiSettings.setAllGesturesEnabled(gesturesEnabled)
        map.uiSettings.isAttributionEnabled = gesturesEnabled
        map.uiSettings.isLogoEnabled = gesturesEnabled
        
        map.setStyle(Style.Builder().fromJson(OSM_STYLE_JSON)) { style ->
            mapStyle = style
            setupUserLocationLayer(style)
            updateUserLocation(style, latestMyLocation)
            map.cameraPosition = CameraPosition.Builder()
                .target(DEFAULT_CENTER)
                .zoom(DEFAULT_ZOOM)
                .build()
            refreshDisplayItems(map, latestPins)
            map.addOnCameraIdleListener {
                refreshDisplayItems(map, latestPins)
            }
            map.addOnCameraMoveListener {
                refreshDisplayItems(map, latestPins)
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
            latestCameraActions.map = null
            mapRef = null
            mapStyle = null
            displayItems = emptyList()
            hasPerformedInitialZoom = false
            mapView.onDestroy()
        }
    }

    LaunchedEffect(mapRef, mapStyle, myLocation) {
        val map = mapRef ?: return@LaunchedEffect
        val location = myLocation ?: return@LaunchedEffect
        if (hasPerformedInitialZoom) return@LaunchedEffect

        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(location, USER_LOCATION_ZOOM),
        )
        Log.d(
            TAG,
            "İlk zoom — lat=${location.latitude}, lng=${location.longitude}, zoom=$USER_LOCATION_ZOOM",
        )
        hasPerformedInitialZoom = true
    }

    DisposableEffect(pins, mapRef, clusterRadiusPx) {
        val map = mapRef
        if (map != null) {
            refreshDisplayItems(map, pins)
        }
        onDispose { }
    }

    DisposableEffect(myLocation, mapStyle) {
        val style = mapStyle
        if (style != null) {
            updateUserLocation(style, myLocation)
        }
        onDispose { }
    }

    DisposableEffect(myLocation, mapRef, followLocationWithPan) {
        val map = mapRef
        val location = myLocation
        if (latestFollowLocationWithPan && location != null && map != null) {
            if (previousFollowLocation == null) {
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(location, latestPinFocusZoom),
                )
            } else if (previousFollowLocation != location) {
                map.animateCamera(CameraUpdateFactory.newLatLng(location), 600)
            }
            previousFollowLocation = location
        }
        onDispose { }
    }

    DisposableEffect(focusMyLocation, myLocation, mapRef) {
        val map = mapRef
        if (latestFocusMyLocation && myLocation != null && map != null) {
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(myLocation, USER_LOCATION_ZOOM),
            )
            latestOnMyLocationFocused()
        }
        onDispose { }
    }

    DisposableEffect(focusVisiblePins, pins, mapRef) {
        val map = mapRef
        if (latestFocusVisiblePins && pins.isNotEmpty() && map != null) {
            focusCameraOnPins(map, pins, latestPinFocusZoom)
            latestOnVisiblePinsFocused()
        }
        onDispose { }
    }

    DisposableEffect(focusSearchArea, searchFocusLocation, mapRef) {
        val map = mapRef
        val location = searchFocusLocation
        if (latestFocusSearchArea && location != null && map != null) {
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(location, SEARCH_AREA_ZOOM),
            )
            latestOnSearchAreaFocused()
        }
        onDispose { }
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { mapView },
            update = { view ->
                view.isFocusable = false
                view.isFocusableInTouchMode = false
            },
        )

        displayItems.forEach { item ->
            when (item) {
                is MapPinDisplayItem.Single -> {
                    MapPinOverlay(
                        pin = item.pin,
                        modifier = Modifier.offset {
                            IntOffset(
                                (item.xPx - 24.dp.toPx()).roundToInt(),
                                (item.yPx - 40.dp.toPx()).roundToInt(),
                            )
                        },
                        onClick = { latestOnPinClick(item.pin.id) },
                    )
                }
                is MapPinDisplayItem.Cluster -> {
                    MapClusterOverlay(
                        count = item.count,
                        modifier = Modifier.offset {
                            IntOffset(
                                (item.xPx - 22.dp.toPx()).roundToInt(),
                                (item.yPx - 22.dp.toPx()).roundToInt(),
                            )
                        },
                        onClick = {
                            val map = mapRef ?: return@MapClusterOverlay
                            forceExpandedPinIds = item.pins.map { it.id }.toSet()
                            expandCluster(map, item)
                            refreshDisplayItems(map, latestPins)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MapClusterOverlay(
    count: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val clusterColor = Color(0xFF2563EB)
    Box(
        modifier = modifier
            .size(44.dp)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(clusterColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count.toString(),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
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

private fun setupUserLocationLayer(style: Style) {
    if (style.getSource(USER_LOCATION_SOURCE_ID) == null) {
        style.addSource(GeoJsonSource(USER_LOCATION_SOURCE_ID))
    }
    if (style.getLayer(USER_LOCATION_LAYER_ID) == null) {
        style.addLayer(
            CircleLayer(USER_LOCATION_LAYER_ID, USER_LOCATION_SOURCE_ID).withProperties(
                PropertyFactory.circleColor(AndroidColor.parseColor("#2563EB")),
                PropertyFactory.circleRadius(10f),
                PropertyFactory.circleStrokeWidth(2f),
                PropertyFactory.circleStrokeColor(AndroidColor.WHITE),
            ),
        )
    }
}

private fun updateUserLocation(style: Style, myLocation: LatLng?) {
    val source = style.getSourceAs<GeoJsonSource>(USER_LOCATION_SOURCE_ID) ?: return
    if (myLocation == null) {
        source.setGeoJson(FeatureCollection.fromFeatures(emptyList()))
    } else {
        source.setGeoJson(Point.fromLngLat(myLocation.longitude, myLocation.latitude))
    }
}

private fun focusCameraOnPins(map: MapLibreMap, pins: List<MapVehiclePin>, zoom: Double) {
    if (pins.isEmpty()) return

    if (pins.size == 1) {
        val pin = pins.first()
        map.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .target(LatLng(pin.latitude, pin.longitude))
                    .zoom(zoom)
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
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), PIN_BOUNDS_PADDING_PX))
    } catch (_: Exception) {
        val first = pins.first()
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(LatLng(first.latitude, first.longitude), zoom),
        )
    }
}

private const val PIN_FOCUS_ZOOM = 13.0
private const val SEARCH_AREA_ZOOM = 13.0
private const val PIN_BOUNDS_PADDING_PX = 120
private const val ZOOM_STEP = 1.0
private const val CLUSTER_RADIUS_DP = 56f
/** Bu zoom ve üzerinde clustering tamamen kapalı; tüm pinler tek tek gösterilir. */
private const val CLUSTER_MAX_ZOOM = 13.0
/** Bu zoom ve üzerinde 2 pin asla kümeleşmez; yalnızca 3+ pin küme olabilir. */
private const val CLUSTER_MIN_COUNT_ZOOM = 11.5
private const val CLUSTER_REFERENCE_ZOOM = 11.0
private const val CLUSTER_ZOOM_STEP = 2.0
private const val CLUSTER_EXPAND_PADDING_PX = 160
private const val CLUSTER_RECLUSTER_ZOOM = 10.5
private const val CLUSTER_SPREAD_ZOOM = 11.0
private const val PIN_SPREAD_SEPARATION_FACTOR = 0.9f

private fun expandCluster(map: MapLibreMap, cluster: MapPinDisplayItem.Cluster) {
    val pins = cluster.pins
    if (pins.isEmpty()) return

    if (pins.size == 1) {
        val pin = pins.first()
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(pin.latitude, pin.longitude),
                CLUSTER_MAX_ZOOM.coerceAtMost(map.maxZoomLevel),
            ),
        )
        return
    }

    val boundsBuilder = LatLngBounds.Builder()
    pins.forEach { pin ->
        boundsBuilder.include(LatLng(pin.latitude, pin.longitude))
    }

    val padding = intArrayOf(
        CLUSTER_EXPAND_PADDING_PX,
        CLUSTER_EXPAND_PADDING_PX,
        CLUSTER_EXPAND_PADDING_PX,
        CLUSTER_EXPAND_PADDING_PX,
    )

    try {
        val fitted = map.getCameraForLatLngBounds(boundsBuilder.build(), padding) ?: throw IllegalStateException()
        val targetZoom = fitted.zoom
            .coerceAtLeast(CLUSTER_MAX_ZOOM)
            .coerceAtMost(map.maxZoomLevel)
        map.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition.Builder()
                    .target(fitted.target)
                    .zoom(targetZoom)
                    .bearing(fitted.bearing)
                    .tilt(fitted.tilt)
                    .build(),
            ),
        )
    } catch (_: Exception) {
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(cluster.centerLat, cluster.centerLng),
                CLUSTER_MAX_ZOOM.coerceAtMost(map.maxZoomLevel),
            ),
        )
    }
}

private fun clusterRadiusForZoom(baseRadiusPx: Float, zoom: Double): Float {
    val scale = 2.0.pow(CLUSTER_REFERENCE_ZOOM - zoom)
    return (baseRadiusPx * scale.toFloat()).coerceIn(baseRadiusPx * 0.08f, baseRadiusPx * 2.5f)
}

private fun clusterPins(
    positions: List<PinScreenPosition>,
    baseRadiusPx: Float,
    zoom: Double,
    forceExpandedPinIds: Set<String> = emptySet(),
): List<MapPinDisplayItem> {
    if (positions.isEmpty()) return emptyList()

    val expandedSingles = positions
        .filter { it.pin.id in forceExpandedPinIds }
        .map { MapPinDisplayItem.Single(it.pin, it.xPx, it.yPx) }

    val clusterable = positions.filter { it.pin.id !in forceExpandedPinIds }
    if (clusterable.isEmpty()) return expandedSingles

    if (zoom >= CLUSTER_MAX_ZOOM) {
        return expandedSingles + clusterable.map { MapPinDisplayItem.Single(it.pin, it.xPx, it.yPx) }
    }

    val radiusPx = clusterRadiusForZoom(baseRadiusPx, zoom)
    val minClusterSize = if (zoom >= CLUSTER_MIN_COUNT_ZOOM) 3 else 2

    val count = clusterable.size
    val parent = IntArray(count) { it }

    fun find(index: Int): Int {
        var root = index
        while (parent[root] != root) {
            root = parent[root]
        }
        var current = index
        while (parent[current] != current) {
            val next = parent[current]
            parent[current] = root
            current = next
        }
        return root
    }

    fun union(first: Int, second: Int) {
        val firstRoot = find(first)
        val secondRoot = find(second)
        if (firstRoot != secondRoot) {
            parent[secondRoot] = firstRoot
        }
    }

    val radiusSquared = radiusPx * radiusPx
    for (first in 0 until count) {
        for (second in first + 1 until count) {
            val deltaX = clusterable[first].xPx - clusterable[second].xPx
            val deltaY = clusterable[first].yPx - clusterable[second].yPx
            if ((deltaX * deltaX) + (deltaY * deltaY) <= radiusSquared) {
                union(first, second)
            }
        }
    }

    val groups = linkedMapOf<Int, MutableList<Int>>()
    for (index in 0 until count) {
        groups.getOrPut(find(index)) { mutableListOf() }.add(index)
    }

    val clusteredItems = groups.values.flatMap { indices ->
        when {
            indices.size == 1 -> {
                val position = clusterable[indices.first()]
                listOf(MapPinDisplayItem.Single(position.pin, position.xPx, position.yPx))
            }
            indices.size < minClusterSize -> {
                indices.map { index ->
                    val position = clusterable[index]
                    MapPinDisplayItem.Single(position.pin, position.xPx, position.yPx)
                }
            }
            else -> {
                val clusterPins = indices.map { clusterable[it].pin }
                val centerX = indices.map { clusterable[it].xPx }.average().toFloat()
                val centerY = indices.map { clusterable[it].yPx }.average().toFloat()
                listOf(
                    MapPinDisplayItem.Cluster(
                        pins = clusterPins,
                        xPx = centerX,
                        yPx = centerY,
                        count = indices.size,
                    ),
                )
            }
        }
    }

    return expandedSingles + clusteredItems
}

private fun applySpiderSpread(
    items: List<MapPinDisplayItem>,
    separationPx: Float,
    zoom: Double,
): List<MapPinDisplayItem> {
    if (zoom < CLUSTER_SPREAD_ZOOM) return items

    val clusters = items.filterIsInstance<MapPinDisplayItem.Cluster>()
    val singles = items.filterIsInstance<MapPinDisplayItem.Single>()
    if (singles.size <= 1) return items

    val spreadSingles = spreadOverlappingPositions(
        positions = singles.map { PinScreenPosition(it.pin, it.xPx, it.yPx) },
        separationPx = separationPx,
    ).map { MapPinDisplayItem.Single(it.pin, it.xPx, it.yPx) }

    return spreadSingles + clusters
}

private fun spreadOverlappingPositions(
    positions: List<PinScreenPosition>,
    separationPx: Float,
): List<PinScreenPosition> {
    if (positions.size <= 1) return positions

    val count = positions.size
    val parent = IntArray(count) { it }

    fun find(index: Int): Int {
        var root = index
        while (parent[root] != root) {
            root = parent[root]
        }
        var current = index
        while (parent[current] != current) {
            val next = parent[current]
            parent[current] = root
            current = next
        }
        return root
    }

    fun union(first: Int, second: Int) {
        val firstRoot = find(first)
        val secondRoot = find(second)
        if (firstRoot != secondRoot) {
            parent[secondRoot] = firstRoot
        }
    }

    val separationSquared = separationPx * separationPx
    for (first in 0 until count) {
        for (second in first + 1 until count) {
            val deltaX = positions[first].xPx - positions[second].xPx
            val deltaY = positions[first].yPx - positions[second].yPx
            if ((deltaX * deltaX) + (deltaY * deltaY) <= separationSquared) {
                union(first, second)
            }
        }
    }

    val groups = linkedMapOf<Int, MutableList<Int>>()
    for (index in 0 until count) {
        groups.getOrPut(find(index)) { mutableListOf() }.add(index)
    }

    val result = positions.toMutableList()
    groups.values.forEach { group ->
        if (group.size <= 1) return@forEach

        val centerX = group.map { positions[it].xPx }.average().toFloat()
        val centerY = group.map { positions[it].yPx }.average().toFloat()
        val radius = separationPx * 0.85f

        group.forEachIndexed { index, positionIndex ->
            val angle = (2.0 * PI * index / group.size) - (PI / 2.0)
            result[positionIndex] = positions[positionIndex].copy(
                xPx = centerX + (radius * cos(angle)).toFloat(),
                yPx = centerY + (radius * sin(angle)).toFloat(),
            )
        }
    }

    return result
}
