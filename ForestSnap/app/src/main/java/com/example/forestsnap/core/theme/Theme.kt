package com.example.forestsnap.core.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Our ForestSnap Colors
val PrimaryGreen = Color(0xFF2E7D32)
val AccentOrange = Color(0xFFE65100)
val SurfaceLight = Color(0xFFF5F5F6)
val SurfaceDark = Color(0xFF121212)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    secondary = AccentOrange,
    surface = SurfaceDark,
    background = Color(0xFF1E1E1E)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    secondary = AccentOrange,
    surface = SurfaceLight,
    background = Color.White
)

@Composable
fun ForestSnapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}