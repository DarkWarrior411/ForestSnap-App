package com.example.forestsnap.features.map

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.forestsnap.features.dashboard.DashboardViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.tileprovider.cachemanager.CacheManager
import android.graphics.Color
import org.osmdroid.views.overlay.Polygon

@Composable
fun MapScreen(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        Configuration.getInstance()
            .load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val currentLat = uiState.currentLat ?: 13.0308
    val currentLng = uiState.currentLng ?: 77.5650
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    val isLocationLocked = !uiState.locationText.contains("Fetching") &&
            !uiState.locationText.contains("Required") &&
            !uiState.locationText.contains("Failed")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Wilderness Map",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Offline Access",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            if (uiState.isMapCached) "Map is ready for offline use." else "Download 30km radius around you.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Button(
                        onClick = {
                            if (isLocationLocked && uiState.isOnline && !uiState.isMapCached) {
                                mapViewRef?.let { map ->
                                    try {
                                        val cacheManager = CacheManager(map)
                                        // 0.27 degrees is roughly 30km at the equator
                                        val radius = 0.27
                                        val boundingBox = org.osmdroid.util.BoundingBox(
                                            currentLat + radius, 
                                            currentLng + radius, 
                                            currentLat - radius, 
                                            currentLng - radius
                                        )
                                        
                                        cacheManager.downloadAreaAsync(
                                            context, 
                                            boundingBox, 
                                            13, 
                                            15, 
                                            object : CacheManager.CacheManagerCallback {
                                                override fun onTaskComplete() {
                                                    coroutineScope.launch {
                                                        viewModel.updateMapCacheStatus(false, 100, true)
                                                        Toast.makeText(context, "Map Downloaded!", Toast.LENGTH_SHORT).show()
                                                    }
                                                }

                                                override fun onTaskFailed(errors: Int) {
                                                    coroutineScope.launch {
                                                        viewModel.updateMapCacheStatus(false, 0, false)
                                                        Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
                                                    }
                                                }

                                                override fun updateProgress(progress: Int, currentZoomLevel: Int, zoomMin: Int, zoomMax: Int) {
                                                    coroutineScope.launch {
                                                        viewModel.updateMapCacheStatus(true, progress, false)
                                                    }
                                                }

                                                override fun downloadStarted() {
                                                    coroutineScope.launch {
                                                        viewModel.updateMapCacheStatus(true, 0, false)
                                                    }
                                                }

                                                override fun setPossibleTilesInArea(total: Int) {}
                                            }
                                        )
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error starting download: ${e.message}", Toast.LENGTH_LONG).show()
                                        viewModel.updateMapCacheStatus(false, 0, false)
                                    }
                                } ?: run {
                                    Toast.makeText(context, "Map not ready yet.", Toast.LENGTH_SHORT).show()
                                }
                            } else if (!uiState.isOnline) {
                                Toast.makeText(context, "Internet required to download.", Toast.LENGTH_SHORT).show()
                            } else if (!isLocationLocked) {
                                Toast.makeText(context, "Waiting for GPS lock...", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !uiState.isMapDownloading && isLocationLocked && !uiState.isMapCached,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (uiState.isMapCached) {
                            Icon(
                                Icons.Default.Done,
                                contentDescription = "Done",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Saved")
                        } else {
                            Icon(
                                Icons.Default.CloudDownload,
                                contentDescription = "Download",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download")
                        }
                    }
                }

                if (uiState.isMapDownloading) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        progress = { uiState.mapDownloadProgress / 100f },
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Downloading tiles: ${uiState.mapDownloadProgress}%",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
        ) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        mapViewRef = this
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(15.0)
                        controller.setCenter(GeoPoint(currentLat, currentLng))

                        val compassOverlay = CompassOverlay(ctx, InternalCompassOrientationProvider(ctx), this)
                        compassOverlay.enableCompass()
                        this.overlays.add(compassOverlay)
                    }
                },
                update = { view ->
                    view.controller.animateTo(GeoPoint(currentLat, currentLng))
                    
                    // 1. Remove old overlays to prevent infinite stacking on UI refresh
                    view.overlays.removeAll { it is Marker || it is Polygon }
                    
                    // 2. Add Heatmap Zones & Markers
                    uiState.mapPins.forEach { pin ->
                        val geoPoint = GeoPoint(pin.latitude, pin.longitude)

                        // --- HEATMAP CIRCLE LOGIC ---
                        val riskCircle = Polygon(view)
                        // 500.0 represents a 500-meter radius around the point. Adjust as needed!
                        riskCircle.points = Polygon.pointsAsCircle(geoPoint, 500.0) 
                        
                        // Dynamic coloring based on risk tier (Semi-transparent)
                        val fillColor = when {
                            pin.final_fire_risk_percent >= 75 -> Color.argb(120, 255, 0, 0)   // Severe: Red
                            pin.final_fire_risk_percent >= 50 -> Color.argb(120, 255, 165, 0) // High: Orange
                            pin.final_fire_risk_percent >= 25 -> Color.argb(120, 255, 255, 0) // Medium: Yellow
                            else -> Color.argb(120, 0, 255, 0)                                // Low: Green
                        }
                        
                        riskCircle.fillPaint.color = fillColor
                        riskCircle.outlinePaint.color = Color.TRANSPARENT // Remove harsh borders
                        riskCircle.outlinePaint.strokeWidth = 0f
                        
                        view.overlays.add(riskCircle)

                        // --- MARKER LOGIC ---
                        val marker = Marker(view)
                        marker.position = geoPoint
                        marker.title = "Risk: ${pin.final_fire_risk_percent}%"
                        marker.snippet = "Fuel Load: ${pin.fuel_load_score}% | Temp: ${pin.temperature_c}°C"
                        // Optional: make the marker icon smaller or slightly transparent so the heatmap shines through
                        // marker.setAlpha(0.8f) 
                        
                        view.overlays.add(marker)
                    }
                    view.invalidate() // Force the map to redraw with new overlays
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}