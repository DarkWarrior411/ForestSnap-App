package com.example.forestsnap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.forestsnap.core.navigation.MainScreen
import com.example.forestsnap.core.theme.ForestSnapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // This is the Compose equivalent of 'runApp()'
        setContent {
            ForestSnapTheme {
                // Launch our Navigation Shell
                MainScreen()
            }
        }
    }
}