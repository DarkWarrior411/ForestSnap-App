package com.example.forestsnap.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.forestsnap.core.utils.PreferenceManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val preferenceManager = remember { PreferenceManager(context) }

    val selectedTheme by preferenceManager.themeFlow.collectAsState(initial = "System Default")
    val localCompressionEnabled by preferenceManager.compressionFlow.collectAsState(initial = true)
    val strictLocationEnabled by preferenceManager.strictLocationFlow.collectAsState(initial = true)
    val offlineModeEnabled by preferenceManager.offlineModeFlow.collectAsState(initial = false)

    var showThemeDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()

            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        item { SettingsSectionTitle("Appearance") }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showThemeDialog = true }
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "App Theme",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            selectedTheme,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        imageVector = when (selectedTheme) {
                            "Light" -> Icons.Default.LightMode
                            "Dark" -> Icons.Default.DarkMode
                            else -> Icons.Default.SettingsBrightness
                        },
                        contentDescription = "Theme Icon",
                        tint = MaterialTheme.colorScheme.primary
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
                onCheckedChange = { isChecked ->
                    coroutineScope.launch {
                        preferenceManager.setCompression(isChecked)
                    }
                }
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        item { SettingsSectionTitle("Data & Connectivity") }
        item {
            SettingsToggleItem(
                title = "Strict Location Requirement",
                description = "Reject all photos that do not contain valid EXIF location data",
                isChecked = strictLocationEnabled,
                onCheckedChange = { isChecked ->
                    coroutineScope.launch {
                        preferenceManager.setStrictLocation(isChecked)
                    }
                }
            )
        }
        item {
            SettingsToggleItem(
                title = "Offline Mode",
                description = "Queue all uploads locally until Wi-Fi is available",
                isChecked = offlineModeEnabled,
                onCheckedChange = { isChecked ->
                    coroutineScope.launch {
                        preferenceManager.setOfflineMode(isChecked)
                    }
                }
            )
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("App Version", fontWeight = FontWeight.Bold)
                        Text("v1.0.0 (Local Build)", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme") },
            containerColor = MaterialTheme.colorScheme.surface,
            text = {
                Column {
                    listOf("System Default", "Light", "Dark").forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        preferenceManager.saveTheme(theme)
                                        showThemeDialog = false
                                    }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedTheme == theme,
                                onClick = {
                                    coroutineScope.launch {
                                        preferenceManager.saveTheme(theme)
                                        showThemeDialog = false
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(theme, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.primary)
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
        color = MaterialTheme.colorScheme.primary,
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
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.surface,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}