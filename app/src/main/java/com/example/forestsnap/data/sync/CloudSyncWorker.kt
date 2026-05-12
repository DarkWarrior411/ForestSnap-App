package com.example.forestsnap.data.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.forestsnap.data.local.SyncSnapDao
import com.example.forestsnap.data.remote.ForestSnapApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@HiltWorker
class CloudSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val dao: SyncSnapDao,
    private val api: ForestSnapApi
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val pendingSnaps = dao.getPendingSnaps().first()

            if (pendingSnaps.isEmpty()) {
                return Result.success()
            }

            for (snap in pendingSnaps) {
                dao.setSyncingStatus(snap.id, true)
                
                val imageFile = File(snap.photoPath)

                if (!imageFile.exists()) {
                    Log.e("CloudSync", "File not found: ${snap.photoPath}. Marking as error.")
                    // Do not mark as synced to prevent silent failure if we actually wanted to retry. 
                    // But if the file is truly gone, we must drop it or mark it as error.
                    // Let's delete it so it stops clogging the queue.
                    dao.deleteSnap(snap.id)
                    continue
                }

                val latBody = snap.latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val lonBody = snap.longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                
                val imagePart = MultipartBody.Part.createFormData(
                    "image",
                    imageFile.name,
                    imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                )

                try {
                    val response = api.analyzeEnvironment(latBody, lonBody, imagePart)
                    Log.i("CloudSync", "Analysis Complete. Risk: ${response.final_fire_risk_percent}%")
                    dao.updateAnalysisAndMarkSynced(
                        snap.id,
                        response.final_fire_risk_percent,
                        response.fuel_load_score,
                        response.dryness_risk_tier
                    )
                } catch (e: retrofit2.HttpException) {
                    Log.e("CloudSync", "Server error: ${e.code()}")
                    if (e.code() >= 500 || e.code() == 408) {
                        dao.setSyncingStatus(snap.id, false)
                        return Result.retry()
                    } else {
                        Log.e("CloudSync", "Unrecoverable error. Dropping snap ${snap.id}.")
                        dao.markAsSynced(snap.id)
                    }
                } catch (e: Exception) {
                    Log.e("CloudSync", "Network error", e)
                    dao.setSyncingStatus(snap.id, false)
                    return Result.retry()
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}