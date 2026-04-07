package com.example.forestsnap.features.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.forestsnap.core.navigation.Screen

@Composable
fun DashboardScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ForestSnap Dashboard",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NavigationCard(
                title = "Camera",
                description = "Capture forest snapshots",
                onClick = { navController.navigate(Screen.Camera.route) }
            )
            
            NavigationCard(
                title = "Map",
                description = "View snapshot locations",
                onClick = { navController.navigate(Screen.Map.route) }
            )
            
            NavigationCard(
                title = "Sync Queue",
                description = "Manage pending syncs",
                onClick = { navController.navigate(Screen.SyncQueue.route) }
            )
            
            NavigationCard(
                title = "Settings",
                description = "App settings",
                onClick = { navController.navigate(Screen.Settings.route) }
            )
        }
    }
}

@Composable
fun NavigationCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge
            )
            Button(
                onClick = onClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Go")
            }
        }
    }
}
