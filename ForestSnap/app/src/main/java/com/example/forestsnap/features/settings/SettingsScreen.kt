package com.example.forestsnap.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var localCompressionEnabled by remember { mutableStateOf(true) }
    var strictLocationEnabled by remember { mutableStateOf(true) }
    var offlineModeEnabled by remember { mutableStateOf(false) }

    // Theme State (Ideally, you'll hook this up to your PreferenceManager later)
    var selectedTheme by remember { mutableStateOf("System Default") }
    var showThemeDialog by remember { mutableStateOf(false) }

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

        // --- NEW: Appearance Section ---
        item { SettingsSectionTitle("Appearance") }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showThemeDialog = true }
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("App Theme", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Text(selectedTheme, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Icon(
                        imageVector = when(selectedTheme) {
                            "Light" -> Icons.Default.LightMode
                            "Dark" -> Icons.Default.DarkMode
                            else -> Icons.Default.SettingsBrightness
                        },
                        contentDescription = "Theme Icon",
                        tint = Color(0xFF2E7D32)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

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

    // --- NEW: Theme Selection Dialog ---
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme") },
            text = {
                Column {
                    val themes = listOf("System Default", "Light", "Dark")
                    themes.forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTheme = theme
                                    showThemeDialog = false
                                    // TODO: Save to PreferenceManager here
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedTheme == theme,
                                onClick = {
                                    selectedTheme = theme
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(theme)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancel", color = Color(0xFF2E7D32))
                }
            }
        )
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