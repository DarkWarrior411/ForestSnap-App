// app/src/main/java/com/example/forestsnap/features/syncqueue/SyncQueueScreen.kt

package com.example.forestsnap.features.syncqueue

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import coil.compose.AsyncImage
import com.example.forestsnap.data.sync.CloudSyncWorker
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SyncQueueScreen(viewModel: SyncQueueViewModel = viewModel()) {
    val queue by viewModel.pendingQueue.collectAsState()
    val context = LocalContext.current
    var selectedImagePath by remember { mutableStateOf<String?>(null) }
    val isOnline = rememberConnectivityState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Sync Queue", style = MaterialTheme.typography.headlineMedium)
                Text(
                    text = if (isOnline) "🟢 Connected" else "🔴 Offline",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isOnline) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    val forceSyncRequest = OneTimeWorkRequestBuilder<CloudSyncWorker>().build()
                    WorkManager.getInstance(context).enqueue(forceSyncRequest)
                },
                enabled = queue.isNotEmpty() && isOnline
            ) {
                Text("Force Sync")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (queue.isEmpty()) {
            Text("No pending snaps. You are all caught up!")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(queue) { snap ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { selectedImagePath = snap.photoPath },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = File(snap.photoPath),
                                contentDescription = "Forest Snap",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text("Snap ID: ${snap.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Time: ${formatTimestamp(snap.timestamp)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Lat: ${String.format("%.4f", snap.latitude)}°", style = MaterialTheme.typography.bodyMedium)
                                Text("Lng: ${String.format("%.4f", snap.longitude)}°", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }

    selectedImagePath?.let { path ->
        Dialog(
            onDismissRequest = { selectedImagePath = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = File(path),
                    contentDescription = "Full Screen Snap",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { selectedImagePath = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun rememberConnectivityState(): Boolean {
    val context = LocalContext.current
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    var isConnected by remember {
        mutableStateOf(connectivityManager.activeNetwork != null)
    }

    DisposableEffect(connectivityManager) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { isConnected = true }
            override fun onLost(network: Network) { isConnected = false }
        }
        val request = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(request, callback)

        onDispose { connectivityManager.unregisterNetworkCallback(callback) }
    }
    return isConnected
}

private fun formatTimestamp(millis: Long): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(millis))
}