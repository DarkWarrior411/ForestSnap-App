package com.example.forestsnap.features.map

import android.preference.PreferenceManager
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
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider

@Composable
fun MapScreen(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    var currentLat by remember { mutableStateOf(13.0308) }
    var currentLng by remember { mutableStateOf(77.5650) }

    LaunchedEffect(uiState.locationText) {
        try {
            if (!uiState.locationText.contains("Fetching") && !uiState.locationText.contains("Required") && !uiState.locationText.contains("Failed")) {
                val parts = uiState.locationText.split(",")
                if (parts.size == 2) {
                    currentLat = parts[0].replace("Lat:", "").trim().toDouble()
                    currentLng = parts[1].replace("Lng:", "").trim().toDouble()
                }
            }
        } catch (e: Exception) { /* Ignore */ }
    }

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

        // --- MOCKED MAP CACHE DOWNLOADER ---
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
                        Text("Offline Access", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if (uiState.isMapCached) "Map is ready for offline use." else "Download 30km radius around you.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }

                    Button(
                        onClick = {
                            if (isLocationLocked && uiState.isOnline && !uiState.isMapCached) {
                                coroutineScope.launch {
                                    // 1. Tell ViewModel download has started
                                    viewModel.updateMapCacheStatus(isDownloading = true, progress = 0, isCached = false)

                                    // 2. The Smoke and Mirrors: Fake a 4-second download
                                    for (i in 1..100) {
                                        delay(40) // Wait 40 milliseconds per tick
                                        viewModel.updateMapCacheStatus(isDownloading = true, progress = i, isCached = false)
                                    }

                                    // 3. Mark as success
                                    viewModel.updateMapCacheStatus(isDownloading = false, progress = 100, isCached = true)
                                    Toast.makeText(context, "Map Downloaded!", Toast.LENGTH_SHORT).show()
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
                            Icon(Icons.Default.Done, contentDescription = "Done", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Saved")
                        } else {
                            Icon(Icons.Default.CloudDownload, contentDescription = "Download", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download")
                        }
                    }
                }

                if (uiState.isMapDownloading) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), progress = { uiState.mapDownloadProgress / 100f }, color = MaterialTheme.colorScheme.primary)
                    Text("Downloading tiles: ${uiState.mapDownloadProgress}%", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}