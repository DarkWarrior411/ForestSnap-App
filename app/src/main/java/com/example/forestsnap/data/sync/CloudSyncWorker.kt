package com.example.forestsnap.data.sync

import android.content.Context
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

/**
 * Hilt-injected CoroutineWorker performing background upload of offline survey snapshots to the edge API.
 */
@HiltWorker
class CloudSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val dao: SyncSnapDao,
    private val api: ForestSnapApi
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            dao.resetStuckSyncStates()
            val pendingSnaps = dao.getPendingSnaps().first()

            if (pendingSnaps.isEmpty()) return Result.success()

            for (snap in pendingSnaps) {
                dao.setSyncingStatus(snap.id, true)
                val imageFile = File(snap.photoPath)

                if (!imageFile.exists()) {
                    if (snap.lastAttemptedAt != null && System.currentTimeMillis() - snap.lastAttemptedAt > 7L * 24 * 60 * 60 * 1000) {
                        dao.deleteSnap(snap.id)
                    } else {
                        dao.markAsError(snap.id, "file_not_found")
                    }
                    continue
                }

                val safeLat = snap.latitude ?: 0.0
                val safeLon = snap.longitude ?: 0.0

                val latBody = safeLat.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val lonBody = safeLon.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                val imagePart = MultipartBody.Part.createFormData(
                    "image",
                    imageFile.name,
                    imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                )

                try {
                    val response = api.analyzeEnvironment(latBody, lonBody, imagePart)
                    dao.updateAnalysisAndMarkSynced(
                        snap.id,
                        response.final_fire_risk_percent,
                        response.fuel_load_score,
                        response.dryness_risk_tier,
                        response.temperature_c,
                        response.humidity_percent,
                        response.wind_speed_ms,
                        response.wind_direction_deg
                    )
                } catch (e: retrofit2.HttpException) {
                    if (e.code() >= 500 || e.code() == 408) {
                        dao.setSyncingStatus(snap.id, false)
                        return Result.retry()
                    } else {
                        dao.markAsError(snap.id, "FAILED_CLIENT_ERROR_${e.code()}")
                    }
                } catch (e: Exception) {
                    dao.setSyncingStatus(snap.id, false)
                    return Result.retry()
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}