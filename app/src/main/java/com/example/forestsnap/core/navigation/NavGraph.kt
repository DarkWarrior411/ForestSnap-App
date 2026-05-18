package com.example.forestsnap.core.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.forestsnap.features.dashboard.CameraScreen
import com.example.forestsnap.features.dashboard.DashboardScreen
import com.example.forestsnap.features.dashboard.DashboardViewModel
import com.example.forestsnap.features.map.LocationPickerScreen
import com.example.forestsnap.features.map.MapScreen
import com.example.forestsnap.features.settings.SettingsScreen
import com.example.forestsnap.features.syncqueue.SyncQueueScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Filled.Home)
    object Map : Screen("map", "Map", Icons.Filled.Map)
    object SyncQueue : Screen("syncqueue", "Sync", Icons.Filled.CloudUpload)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
    object Camera : Screen("camera", "Camera", Icons.Filled.CameraAlt)

    object LocationPicker : Screen("location_picker/{uri}", "Pick Location", Icons.Filled.Place) {
        fun createRoute(uri: String) = "location_picker/${Uri.encode(uri)}"
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val sharedDashboardViewModel: DashboardViewModel = hiltViewModel()
    val context = LocalContext.current

    val items = listOf(
        Screen.Dashboard,
        Screen.Map,
        Screen.SyncQueue,
        Screen.Settings
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        val showBottomBar = currentDestination?.route in items.map { it.route }

        Scaffold(
            bottomBar = {
                AnimatedVisibility(
                    visible = showBottomBar,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ) {
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
            }
        ) { innerPadding ->

            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = { fadeIn(animationSpec = tween(250)) },
                exitTransition = { fadeOut(animationSpec = tween(250)) },
                popEnterTransition = { fadeIn(animationSpec = tween(250)) },
                popExitTransition = { fadeOut(animationSpec = tween(250)) }
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                        onNavigateToLocationPicker = { uri ->
                            navController.navigate(Screen.LocationPicker.createRoute(uri))
                        },
                        viewModel = sharedDashboardViewModel
                    )
                }
                composable(Screen.Map.route) {
                    MapScreen(sharedDashboardViewModel)
                }
                composable(Screen.SyncQueue.route) {
                    SyncQueueScreen()
                }
                composable(Screen.Settings.route) {
                    SettingsScreen()
                }
                composable(Screen.Camera.route) {
                    CameraScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.LocationPicker.route) { backStackEntry ->
                    val encodedUri = backStackEntry.arguments?.getString("uri") ?: return@composable
                    val photoUri = Uri.parse(Uri.decode(encodedUri))

                    LocationPickerScreen(
                        photoUri = photoUri,
                        onLocationConfirmed = { returnedUri, lat, lon ->
                            sharedDashboardViewModel.processGalleryPhoto(
                                context,
                                returnedUri,
                                lat,
                                lon
                            )
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}