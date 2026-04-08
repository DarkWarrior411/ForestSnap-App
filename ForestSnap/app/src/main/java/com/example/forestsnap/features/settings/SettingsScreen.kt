package com.example.forestsnap.features.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    var localCompressionEnabled by remember { mutableStateOf(true) }
    var strictLocationEnabled by remember { mutableStateOf(true) }
    var offlineModeEnabled by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        item { SettingsSectionTitle("Image Processing") }
        item {
            SettingsToggleItem(
                title = "Local Image Compression",
                description = "Compress images on device to save storage space",
                isChecked = localCompressionEnabled,
                onCheckedChange = { localCompressionEnabled = it }
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item { SettingsSectionTitle("Data & Connectivity") }
        item {
            SettingsToggleItem(
                title = "Strict Location Requirement",
                description = "Reject all photos that do not contain valid EXIF location data",
                isChecked = strictLocationEnabled,
                onCheckedChange = { strictLocationEnabled = it }
            )
        }
        item {
            SettingsToggleItem(
                title = "Offline Mode",
                description = "Queue all uploads locally until Wi-Fi is available",
                isChecked = offlineModeEnabled,
                onCheckedChange = { offlineModeEnabled = it }
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("App Version", fontWeight = FontWeight.Bold)
                        Text("v1.0.0 (Local Build)", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = Color(0xFF2E7D32),
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
    )
}

@Composable
fun SettingsToggleItem(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF2E7D32)
            )
        )
    }
}