// app/src/main/java/com/example/forestsnap/data/sync/CloudSyncWorker.kt

package com.example.forestsnap.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.forestsnap.data.local.ForestDatabase
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

class CloudSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    // Configure client with generous timeouts for edge-inference processing
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun doWork(): Result {
        val database = ForestDatabase.getDatabase(applicationContext)
        val dao = database.syncSnapDao()

        return try {
            val pendingSnaps = dao.getPendingSnaps().first()

            if (pendingSnaps.isEmpty()) {
                return Result.success()
            }

            // TODO: Replace with your actual Edge Server IP or URL
            val serverUrl = "http://10.20.34.117:8000/analyze"

            for (snap in pendingSnaps) {
                val imageFile = File(snap.photoPath)
                
                if (!imageFile.exists()) {
                    Log.e("CloudSync", "File not found: ${snap.photoPath}")
                    dao.markAsSynced(snap.id) // Skip missing files to avoid blocking queue
                    continue
                }

                // 1. Build the Multipart Form Data matching FastAPI expectations
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("lat", snap.latitude.toString())
                    .addFormDataPart("lon", snap.longitude.toString())
                    .addFormDataPart(
                        "image", 
                        imageFile.name, 
                        imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    )
                    .build()

                // 2. Build the Request
                val request = Request.Builder()
                    .url(serverUrl)
                    .post(requestBody)
                    .build()

                // 3. Execute the network call
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    
                    // Optional: Parse the JSON response to log the Fire Risk
                    responseData?.let {
                        val json = JSONObject(it)
                        val riskScore = json.getDouble("final_fire_risk_percent")
                        Log.i("CloudSync", "Analysis Complete. Risk: $riskScore%")
                    }

                    // 4. Mark as synced in local DB
                    dao.markAsSynced(snap.id)
                } else {
                    Log.e("CloudSync", "Server error: ${response.code}")
                    return Result.retry() // Backoff and try again later
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry() // Network dropped or server unreachable, retry later
        }
    }
}