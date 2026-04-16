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

        // --- CRITICAL FIX 1: OSMDroid Storage Permissions ---
        // Forces OSMDroid to use internal app storage, preventing SecurityException crashes on Android 10+
        val osmConfig = Configuration.getInstance()
        osmConfig.userAgentValue = packageName
        val basePath = File(cacheDir.absolutePath, "osmdroid")
        osmConfig.osmdroidBasePath = basePath
        val tileCache = File(osmConfig.osmdroidBasePath, "tile")
        osmConfig.osmdroidTileCache = tileCache

        setContent {
            // --- CRITICAL FIX 2: Dark Mode Hookup ---
            val preferenceManager = remember { PreferenceManager(applicationContext) }
            val savedTheme by preferenceManager.themeFlow.collectAsState(initial = "System Default")

            val darkTheme = when (savedTheme) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            // The app is now wrapped in the dynamic theme state!
            ForestSnapTheme(darkTheme = darkTheme) {
                MainScreen()
            }
        }
    }
}