package com.example.forestsnap.features.map

import android.view.Gravity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Park
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.forestsnap.features.dashboard.DashboardViewModel
import com.example.forestsnap.features.dashboard.RiskFilter
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.expressions.dsl.generated.step
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.addLayerAbove
import com.mapbox.maps.extension.style.layers.addLayerBelow
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.layers.generated.heatmapLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.layers.properties.generated.Visibility
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.compass.compass
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.logo.logo

/**
 * Native Mapbox screen displaying risk heatmaps, forest reserves, FIRMS fires, and wind spread fan cones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    val currentLat = uiState.currentLat ?: 13.0308
    val currentLng = uiState.currentLng ?: 77.5650
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapViewRef?.onStart()
                Lifecycle.Event.ON_STOP -> mapViewRef?.onStop()
                Lifecycle.Event.ON_DESTROY -> mapViewRef?.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    mapViewRef = this
                    compass.updateSettings {
                        enabled = true
                        position = Gravity.BOTTOM or Gravity.END
                        marginBottom = 200f
                        marginRight = 32f
                    }
                    logo.updateSettings {
                        position = Gravity.BOTTOM or Gravity.START
                        marginBottom = 100f
                    }
                    attribution.updateSettings {
                        position = Gravity.BOTTOM or Gravity.START
                        marginBottom = 32f
                    }
                    location.updateSettings { enabled = true }

                    mapboxMap.loadStyle(Style.DARK) { style ->

                        style.addSource(geoJsonSource("boundaries-source") { data("{}") })
                        style.addLayerBelow(fillLayer("boundaries-fill", "boundaries-source") {
                            fillColor(android.graphics.Color.parseColor("#059669"))
                            fillOpacity(0.2)
                        }, "waterway-label")
                        style.addLayerAbove(lineLayer("boundaries-line", "boundaries-source") {
                            lineColor(android.graphics.Color.parseColor("#10b981"))
                            lineWidth(2.0)
                            lineDasharray(listOf(2.0, 2.0))
                        }, "boundaries-fill")

                        style.addSource(geoJsonSource("ml-grid-source") { data("{}") })
                        style.addLayerBelow(fillLayer("ml-grid-fill", "ml-grid-source") {
                            fillColor(
                                step {
                                    get("risk")
                                    literal("#22c55e")
                                    stop { literal(25.0); literal("#eab308") }
                                    stop { literal(50.0); literal("#f97316") }
                                    stop { literal(75.0); literal("#ef4444") }
                                }
                            )
                            fillOpacity(0.4)
                        }, "boundaries-fill")

                        style.addSource(geoJsonSource("firms-source") { data("{}") })
                        style.addLayer(heatmapLayer("firms-heat", "firms-source") {
                            heatmapWeight(interpolate {
                                linear(); get("brightness")
                                stop { literal(300); literal(0) }
                                stop { literal(400); literal(1) }
                            })
                            heatmapIntensity(1.5)
                            heatmapRadius(20.0)
                            heatmapOpacity(0.7)
                        })

                        style.addSource(geoJsonSource("cones-source") { data("{}") })
                        style.addLayer(fillLayer("cones-fill", "cones-source") {
                            fillColor(android.graphics.Color.parseColor("#FF5722"))
                            fillOpacity(0.7)
                        })
                        style.addLayerAbove(lineLayer("cones-line", "cones-source") {
                            lineColor(android.graphics.Color.parseColor("#ffffff"))
                            lineWidth(1.5)
                        }, "cones-fill")

                        mapboxMap.setCamera(
                            CameraOptions.Builder()
                                .center(Point.fromLngLat(currentLng, currentLat))
                                .zoom(14.0)
                                .build()
                        )
                    }
                }
            },
            update = { view ->
                val showBoundaries = uiState.isForestBoundariesVisible
                val showFirms = uiState.isFirmsLayerVisible
                val showHeatmap = uiState.isHeatmapVisible
                val boundariesData = uiState.forestBoundaries
                val firmsData = uiState.firmsPins
                val heatmapData = uiState.heatmapData
                val mapPinsData = uiState.filteredMapPins

                view.mapboxMap.getStyle { style ->

                    val boundaryVisibility =
                        if (showBoundaries) Visibility.VISIBLE else Visibility.NONE
                    style.getLayer("boundaries-fill")?.visibility(boundaryVisibility)
                    style.getLayer("boundaries-line")?.visibility(boundaryVisibility)

                    if (showBoundaries && boundariesData != null) {
                        val features = boundariesData.features.map { customFeature ->
                            val mapboxPoints = customFeature.geometry.coordinates[0].map { coord ->
                                Point.fromLngLat(coord[0], coord[1])
                            }
                            Feature.fromGeometry(Polygon.fromLngLats(listOf(mapboxPoints)))
                        }
                        style.getSourceAs<GeoJsonSource>("boundaries-source")
                            ?.featureCollection(FeatureCollection.fromFeatures(features))
                    }

                    val gridVisibility = if (showHeatmap) Visibility.VISIBLE else Visibility.NONE
                    style.getLayer("ml-grid-fill")?.visibility(gridVisibility)

                    if (showHeatmap) {
                        val gridFeatures = heatmapData.map { square ->
                            val half = square.grid_size / 2.0
                            val ring = listOf(
                                Point.fromLngLat(
                                    square.center_lon - half,
                                    square.center_lat - half
                                ),
                                Point.fromLngLat(
                                    square.center_lon - half,
                                    square.center_lat + half
                                ),
                                Point.fromLngLat(
                                    square.center_lon + half,
                                    square.center_lat + half
                                ),
                                Point.fromLngLat(
                                    square.center_lon + half,
                                    square.center_lat - half
                                ),
                                Point.fromLngLat(square.center_lon - half, square.center_lat - half)
                            )
                            val props =
                                JsonObject().apply { add("risk", JsonPrimitive(square.avg_risk)) }
                            Feature.fromGeometry(Polygon.fromLngLats(listOf(ring)), props)
                        }
                        style.getSourceAs<GeoJsonSource>("ml-grid-source")
                            ?.featureCollection(FeatureCollection.fromFeatures(gridFeatures))
                    }

                    val firmsVisibility = if (showFirms) Visibility.VISIBLE else Visibility.NONE
                    style.getLayer("firms-heat")?.visibility(firmsVisibility)

                    if (showFirms) {
                        val features = firmsData.map { pin ->
                            val properties = JsonObject().apply {
                                add(
                                    "brightness",
                                    JsonPrimitive(pin.brightness)
                                )
                            }
                            Feature.fromGeometry(
                                Point.fromLngLat(pin.longitude, pin.latitude),
                                properties
                            )
                        }
                        style.getSourceAs<GeoJsonSource>("firms-source")
                            ?.featureCollection(FeatureCollection.fromFeatures(features))
                    }

                    val conesVisibility = if (!showHeatmap) Visibility.VISIBLE else Visibility.NONE
                    style.getLayer("cones-fill")?.visibility(conesVisibility)
                    style.getLayer("cones-line")?.visibility(conesVisibility)

                    if (!showHeatmap) {
                        val coneFeatures = mapPinsData.map { pin ->
                            val points = getSpreadConePoints(
                                Point.fromLngLat(pin.longitude, pin.latitude),
                                pin.wind_speed_ms,
                                pin.wind_direction_deg
                            )
                            Feature.fromGeometry(Polygon.fromLngLats(listOf(points)))
                        }
                        style.getSourceAs<GeoJsonSource>("cones-source")
                            ?.featureCollection(FeatureCollection.fromFeatures(coneFeatures))
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 8.dp
        ) {
            LazyRow(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.isHeatmapVisible,
                        onClick = { viewModel.toggleHeatmapLayer() },
                        label = { Text("Risk Grid") },
                        leadingIcon = {
                            if (uiState.isHeatmapVisible) Icon(
                                Icons.Default.Map,
                                null,
                                Modifier.size(16.dp)
                            )
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.isForestBoundariesVisible,
                        onClick = { viewModel.toggleBoundariesLayer() },
                        label = { Text("Borders") },
                        leadingIcon = {
                            if (uiState.isForestBoundariesVisible) Icon(
                                Icons.Default.Park,
                                null,
                                Modifier.size(16.dp)
                            )
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.isFirmsLayerVisible,
                        onClick = { viewModel.toggleFirmsLayer() },
                        label = { Text("NASA") },
                        leadingIcon = {
                            if (uiState.isFirmsLayerVisible) Icon(
                                Icons.Default.LocalFireDepartment,
                                null,
                                Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = uiState.activeAlerts.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 76.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
        ) {
            val topAlert = uiState.activeAlerts.firstOrNull()
            if (topAlert != null) {
                Card(
                    modifier = Modifier.clickable {
                        mapViewRef?.mapboxMap?.flyTo(
                            CameraOptions.Builder()
                                .center(Point.fromLngLat(topAlert.lon, topAlert.lat))
                                .zoom(15.0)
                                .build()
                        )
                    },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Alert",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                topAlert.title,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                topAlert.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { viewModel.toggleListVisibility(true) },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Show List")
        }

        if (uiState.isListVisible) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.toggleListVisibility(false) },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        "Analyzed Locations",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    LazyRow(
                        modifier = Modifier.padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(RiskFilter.values()) { filter ->
                            FilterChip(
                                selected = uiState.selectedFilter == filter,
                                onClick = { viewModel.updateRiskFilter(filter) },
                                label = {
                                    Text(
                                        filter.name.lowercase().replaceFirstChar { it.uppercase() })
                                }
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(uiState.filteredMapPins) { pin ->
                            val riskColor = when {
                                pin.final_fire_risk_percent >= 75 -> Color(0xFFD32F2F)
                                pin.final_fire_risk_percent >= 50 -> Color(0xFFF57C00)
                                pin.final_fire_risk_percent >= 25 -> Color(0xFFFBC02D)
                                else -> Color(0xFF388E3C)
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        mapViewRef?.mapboxMap?.flyTo(
                                            CameraOptions.Builder()
                                                .center(
                                                    Point.fromLngLat(
                                                        pin.longitude,
                                                        pin.latitude
                                                    )
                                                )
                                                .zoom(16.0)
                                                .build()
                                        )
                                        viewModel.toggleListVisibility(false)
                                    },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(riskColor)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Risk: ${pin.final_fire_risk_percent}%",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Fuel: ${pin.fuel_load_score}% | Temp: ${pin.temperature_c}°C",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Icon(
                                        Icons.Default.LocationSearching,
                                        contentDescription = "Locate",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Calculate polygon vertex points defining fire spread fan vectors based on wind direction and velocity. */
fun getSpreadConePoints(center: Point, windSpeed: Double, windDirOrigin: Int?): List<Point> {
    val baseRadius = 500.0
    val earthRadius = 6378137.0
    val points = mutableListOf<Point>()

    if (windDirOrigin == null || windSpeed < 1.0) {
        for (angle in 0..360 step 10) {
            val angleRad = Math.toRadians(angle.toDouble())
            val latOffset = (baseRadius * Math.cos(angleRad)) / earthRadius
            val lonOffset =
                (baseRadius * Math.sin(angleRad)) / (earthRadius * Math.cos(Math.toRadians(center.latitude())))
            points.add(
                Point.fromLngLat(
                    center.longitude() + Math.toDegrees(lonOffset),
                    center.latitude() + Math.toDegrees(latOffset)
                )
            )
        }
        points.add(points.first())
        return points
    }

    val spreadDirRad = Math.toRadians(((windDirOrigin + 180) % 360).toDouble())
    val maxElongation = baseRadius + (windSpeed * 100.0)

    for (angle in 0..360 step 10) {
        val angleRad = Math.toRadians(angle.toDouble())
        val angleDiff = Math.cos(angleRad - spreadDirRad)
        val dynamicRadius = if (angleDiff > 0) {
            baseRadius + (maxElongation - baseRadius) * angleDiff
        } else {
            baseRadius * (1.0 + (angleDiff * 0.5))
        }
        val latOffset = (dynamicRadius * Math.cos(angleRad)) / earthRadius
        val lonOffset =
            (dynamicRadius * Math.sin(angleRad)) / (earthRadius * Math.cos(Math.toRadians(center.latitude())))
        points.add(
            Point.fromLngLat(
                center.longitude() + Math.toDegrees(lonOffset),
                center.latitude() + Math.toDegrees(latOffset)
            )
        )
    }
    points.add(points.first())
    return points
}