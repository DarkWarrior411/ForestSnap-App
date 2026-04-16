package com.example.forestsnap.features.map

import android.preference.PreferenceManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
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

    // Initialize OSMDroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    var currentLat by remember { mutableStateOf(13.0308) }
    var currentLng by remember { mutableStateOf(77.5650) }

    LaunchedEffect(uiState.locationText) {
        try {
            if (!uiState.locationText.contains("Fetching") && !uiState.locationText.contains("Required")) {
                val parts = uiState.locationText.split(",")
                if (parts.size == 2) {
                    currentLat = parts[0].replace("Lat:", "").trim().toDouble()
                    currentLng = parts[1].replace("Lng:", "").trim().toDouble()
                }
            }
        } catch (e: Exception) { /* Ignore */ }
    }

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

        // Map Wrapper
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

                        // Hardware Compass Overlay
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