package com.example.forestsnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.forestsnap.core.navigation.MainScreen
import com.example.forestsnap.core.theme.ForestSnapTheme
import com.example.forestsnap.core.utils.PreferenceManager
import org.osmdroid.config.Configuration
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- CRITICAL FIX: OSMDroid Storage Routing ---
        // Force OSMDroid to use the app's private internal storage instead of the restricted public SD card.
        val osmConfig = Configuration.getInstance()
        osmConfig.userAgentValue = packageName

        val basePath = File(cacheDir.absolutePath, "osmdroid")
        osmConfig.osmdroidBasePath = basePath

        val tileCache = File(osmConfig.osmdroidBasePath, "tile")
        osmConfig.osmdroidTileCache = tileCache

        setContent {
            // 1. Initialize the PreferenceManager
            val preferenceManager = remember { PreferenceManager(applicationContext) }

            // 2. Observe the DataStore Flow
            val savedTheme by preferenceManager.themeFlow.collectAsState(initial = "System Default")

            // 3. Evaluate the boolean for our Theme wrapper
            val darkTheme = when (savedTheme) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            // 4. Wrap the app
            ForestSnapTheme(darkTheme = darkTheme) {
                MainScreen()
            }
        }
    }
}