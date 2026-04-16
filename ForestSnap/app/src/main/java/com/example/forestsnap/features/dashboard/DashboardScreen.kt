package com.example.forestsnap.features.dashboard

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

// OSMDroid imports for caching
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLocationWarning by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullToRefreshState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // --- Checklist States & Logic ---
    var currentLat by remember { mutableStateOf(13.0308) }
    var currentLng by remember { mutableStateOf(77.5650) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0) }
    var isMapCached by remember { mutableStateOf(false) }

    // Update coordinates when ViewModel gets a lock
    LaunchedEffect(uiState.locationText) {
        try {
            if (!uiState.locationText.contains("Fetching") && !uiState.locationText.contains("Required") && !uiState.locationText.contains("Failed")) {
                val parts = uiState.locationText.split(",")
                if (parts.size == 2) {
                    currentLat = parts[0].replace("Lat:", "").trim().toDouble()
                    currentLng = parts[1].replace("Lng:", "").trim().toDouble()
                }
            }
        } catch (e: Exception) { /* Keep defaults if parsing fails */ }
    }

    val isLocationLocked = !uiState.locationText.contains("Fetching") &&
            !uiState.locationText.contains("Required") &&
            !uiState.locationText.contains("Failed")

    // --- Permissions ---
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            viewModel.refreshData()
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refreshData() },
        state = pullRefreshState,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Status Indicator (Online/Offline) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (uiState.isOnline) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.isOnline) "Online" else "Offline",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (uiState.isOnline) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                )
            }

            // --- THE TRAILHEAD CHECKLIST ---
            AnimatedVisibility(visible = !isMapCached) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Pre-Trek Checklist", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Complete before losing Wi-Fi/Cellular", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))

                        Spacer(modifier = Modifier.height(12.dp))

                        // Step 1: GPS Lock
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                            Icon(
                                imageVector = if (isLocationLocked) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = "GPS Status",
                                tint = if (isLocationLocked) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isLocationLocked) "GPS Locked" else "Waiting for GPS Lock...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Step 2: Download Map
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isMapCached) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = "Cache Status",
                                    tint = if (isMapCached) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cache 30km Radius", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Button(
                                onClick = {
                                    if (isLocationLocked && uiState.isOnline) {
                                        coroutineScope.launch {
                                            isDownloading = true

                                            // Calculate 30km Bounding Box
                                            val boundingBox = BoundingBox(
                                                currentLat + 0.27, currentLng + 0.27, currentLat - 0.27, currentLng - 0.27
                                            )
                                            val mapView = MapView(context)
                                            mapView.setTileSource(TileSourceFactory.MAPNIK)
                                            val cacheManager = CacheManager(mapView)

                                            // --- HEADLESS DOWNLOAD FIX ---
                                            cacheManager.downloadAreaAsyncNoUI(context, boundingBox, 10, 15, object : CacheManager.CacheManagerCallback {
                                                override fun onTaskComplete() {
                                                    isDownloading = false
                                                    isMapCached = true
                                                    Toast.makeText(context, "Wilderness Map Ready!", Toast.LENGTH_SHORT).show()
                                                }
                                                override fun onTaskFailed(errors: Int) {
                                                    isDownloading = false
                                                    Toast.makeText(context, "Download failed.", Toast.LENGTH_SHORT).show()
                                                }
                                                override fun updateProgress(progress: Int, currentZoomLevel: Int, zoomMin: Int, zoomMax: Int) {
                                                    downloadProgress = progress
                                                }
                                                override fun downloadStarted() {}
                                                override fun setPossibleTilesInArea(total: Int) {}
                                            })
                                        }
                                    } else if (!uiState.isOnline) {
                                        Toast.makeText(context, "Connect to internet to download.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Waiting for GPS lock...", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = !isDownloading && isLocationLocked,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = "Download", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download")
                            }
                        }

                        if (isDownloading) {
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), progress = { downloadProgress / 100f }, color = MaterialTheme.colorScheme.primary)
                            Text("Downloading tiles: $downloadProgress%", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }

            // --- Warning Banner ---
            if (showLocationWarning) {
                LocationWarningBanner(onDismiss = { showLocationWarning = false })
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- Theme Aware Data Containers ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardCard(
                    title = "Location",
                    value = uiState.locationText,
                    icon = Icons.Default.LocationOn,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )

                DashboardCard(
                    title = "Weather",
                    value = uiState.weatherText,
                    icon = Icons.Default.Cloud,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )

                DashboardCard(
                    title = "Risk Level",
                    value = uiState.riskLevel,
                    icon = Icons.Default.Warning,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Action Buttons ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { navController.navigate("camera") },
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Camera", modifier = Modifier.padding(end = 8.dp))
                    Text("Camera")
                }

                Button(
                    onClick = { showLocationWarning = true }, // Simulated failure
                    modifier = Modifier.weight(1f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", modifier = Modifier.padding(end = 8.dp))
                    Text("Gallery")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DashboardCard(title: String, value: String, icon: ImageVector, containerColor: Color, contentColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor, contentColor = contentColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LocationWarningBanner(onDismiss: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = "Warning Icon")
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Image Not Accepted", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text("This image is missing required location data. Please try uploading another image.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}