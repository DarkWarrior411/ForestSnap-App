package com.example.forestsnap.features.map

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.gestures.addOnMapClickListener

@Composable
fun LocationPickerScreen(
    photoUri: Uri,
    onLocationConfirmed: (Uri, Double, Double) -> Unit
) {
    var selectedPoint by remember { mutableStateOf<Point?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    // FIXED: Use the property accessor instead of deprecated getMapboxMap()
                    mapboxMap.loadStyle(Style.SATELLITE_STREETS)
                    mapboxMap.addOnMapClickListener { point ->
                        selectedPoint = point
                        true
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (selectedPoint != null) {
            Button(
                onClick = {
                    onLocationConfirmed(
                        photoUri,
                        selectedPoint!!.latitude(),
                        selectedPoint!!.longitude()
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
                    .fillMaxWidth(0.8f)
                    .height(56.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm Location")
            }
        } else {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text("Tap the map where you took this photo", modifier = Modifier.padding(16.dp))
            }
        }
    }
}