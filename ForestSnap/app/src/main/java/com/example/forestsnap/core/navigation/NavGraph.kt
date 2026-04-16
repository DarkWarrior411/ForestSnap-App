package com.example.forestsnap.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.forestsnap.core.components.EarthLoader
import com.example.forestsnap.features.dashboard.CameraScreen
import com.example.forestsnap.features.dashboard.DashboardScreen
import com.example.forestsnap.features.dashboard.DashboardViewModel
import com.example.forestsnap.features.map.MapScreen
import com.example.forestsnap.features.settings.SettingsScreen
import com.example.forestsnap.features.syncqueue.SyncQueueScreen
import kotlinx.coroutines.delay

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Filled.Home)
    object Map : Screen("map", "Map", Icons.Filled.Map)
    object SyncQueue : Screen("syncqueue", "Sync", Icons.Filled.CloudUpload)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
    object Camera : Screen("camera", "Camera", Icons.Filled.CameraAlt)
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val sharedLocationViewModel: DashboardViewModel = viewModel()

    val items = listOf(
        Screen.Dashboard,
        Screen.Map,
        Screen.SyncQueue,
        Screen.Settings
    )

    // --- NEW: Surface forces the entire app background to respect Dark/Light Mode ---
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Dashboard.route) { DashboardScreen(navController, sharedLocationViewModel) }
                composable(Screen.Map.route) { TabLoadingWrapper("Loading Map Data...") { MapScreen(sharedLocationViewModel) } }
                composable(Screen.SyncQueue.route) { TabLoadingWrapper("Checking Sync Queue...") { SyncQueueScreen() } }
                composable(Screen.Settings.route) { TabLoadingWrapper("Loading Preferences...") { SettingsScreen() } }
                composable(Screen.Camera.route) { CameraScreen(navController) }
            }
        }
    }
}
@Composable
fun TabLoadingWrapper(
    loadingText: String,
    content: @Composable () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        delay(600)
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                EarthLoader()
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = loadingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    } else {
        content()
    }
}