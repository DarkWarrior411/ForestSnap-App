package com.example.forestsnap.features.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.forestsnap.core.navigation.Screen

@Composable
fun SettingsScreen(navController: NavController) {
    val (autoSyncEnabled, setAutoSyncEnabled) = remember { mutableStateOf(true) }
    val (highQualityPhotos, setHighQualityPhotos) = remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            SettingCard(
                title = "Auto Sync",
                description = "Automatically sync photos when connected",
                isEnabled = autoSyncEnabled,
                onToggle = { setAutoSyncEnabled(it) }
            )
            
            SettingCard(
                title = "High Quality Photos",
                description = "Capture photos in high quality (uses more storage)",
                isEnabled = highQualityPhotos,
                onToggle = { setHighQualityPhotos(it) }
            )
        }
        
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { /* Clear cache */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear Cache")
            }
            
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Dashboard")
            }
        }
    }
}

@Composable
fun SettingCard(
    title: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
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
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.SpaceBetween
            ) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle
                )
            }
        }
    }
}
