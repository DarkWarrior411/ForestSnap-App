// app/src/main/java/com/example/forestsnap/core/theme/Theme.kt

package com.example.forestsnap.core.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val PrimaryGreen = Color(0xFF2E7D32)
val AccentOrange = Color(0xFFE65100)
val SurfaceLight = Color(0xFFF5F5F6)
val SurfaceDark = Color(0xFF121212)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen,
    secondary = AccentOrange,
    surface = SurfaceDark,
    background = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFF2D2D2D),
    onSurfaceVariant = Color.White,
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color.White,
    secondaryContainer = Color(0xFF263238),
    onSecondaryContainer = Color.White,
    tertiaryContainer = Color(0xFF3E2723),
    onTertiaryContainer = Color.White,
    errorContainer = Color(0xFFB71C1C),
    onErrorContainer = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    secondary = AccentOrange,
    surface = SurfaceLight,
    background = Color.White,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color.Black,
    primaryContainer = Color(0xFFE8F5E9),
    onPrimaryContainer = Color(0xFF1B5E20),
    secondaryContainer = Color(0xFFE3F2FD),
    onSecondaryContainer = Color(0xFF0D47A1),
    tertiaryContainer = Color(0xFFFFF3E0),
    onTertiaryContainer = Color(0xFFE65100),
    errorContainer = Color(0xFFFCE4EC),
    onErrorContainer = Color(0xFFB71C1C)
)

// --- CRITICAL FIX: Safe Context Unwrapper ---
// This safely digs through Compose wrappers to find the true Activity
tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun ForestSnapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            // Use the safe unwrapper instead of a direct cast
            val activity = view.context.findActivity()
            if (activity != null) {
                val window = activity.window
                window.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}