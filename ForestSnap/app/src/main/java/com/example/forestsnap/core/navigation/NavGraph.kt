package com.example.forestsnap.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.forestsnap.core.components.EarthLoader

// Define our routes and icons
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Filled.Home)
    object Map : Screen("map", "Map", Icons.Filled.Map)
    object SyncQueue : Screen("syncqueue", "Sync", Icons.Filled.CloudUpload)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    
    val items = listOf(
        Screen.Dashboard,
        Screen.Map,
        Screen.SyncQueue,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination to avoid building up a huge back stack
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true // This ensures tabs remember their state (like IndexedStack!)
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
            // We will connect these to your actual screens soon!
            // For now, we use simple text placeholders so it compiles.
            composable(Screen.Dashboard.route) { PlaceholderScreen("Dashboard") }
            composable(Screen.Map.route) { PlaceholderScreen("Map View") }
            composable(Screen.SyncQueue.route) { PlaceholderScreen("Sync Queue") }
            composable(Screen.Settings.route) { PlaceholderScreen("Settings") }
        }
    }
}

// A temporary screen just to prove navigation works
@Composable
fun PlaceholderScreen(title: String) {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.foundation.layout.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            EarthLoader()
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Developing: $title", 
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = androidx.compose.ui.graphics.Color.Gray
            )
        }
    }
}