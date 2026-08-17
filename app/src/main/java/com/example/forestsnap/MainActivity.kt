package com.example.forestsnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.forestsnap.core.navigation.MainScreen
import com.example.forestsnap.core.theme.ForestSnapTheme
import com.example.forestsnap.core.utils.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main Activity entry point managing theme preferences and Jetpack Compose content view hierarchy.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val savedTheme by preferenceManager.themeFlow.collectAsState(initial = "System Default")

            val darkTheme = when (savedTheme) {
                "Light" -> false
                "Dark" -> true
                else -> isSystemInDarkTheme()
            }

            ForestSnapTheme(darkTheme = darkTheme) {
                MainScreen()
            }
        }
    }
}