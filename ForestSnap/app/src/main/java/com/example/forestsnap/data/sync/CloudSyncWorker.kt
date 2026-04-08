// app/src/main/java/com/example/forestsnap/data/sync/CloudSyncWorker.kt

package com.example.forestsnap.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.forestsnap.data.local.ForestDatabase
import kotlinx.coroutines.flow.first

class CloudSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = ForestDatabase.getDatabase(applicationContext)
        val dao = database.syncSnapDao()

        return try {
            val pendingSnaps = dao.getPendingSnaps().first()

            if (pendingSnaps.isEmpty()) {
                return Result.success()
            }

            for (snap in pendingSnaps) {
                // TODO: In Phase 2, we will replace this delay with actual AWS S3 upload logic
                kotlinx.coroutines.delay(1000)

                // Once "uploaded", mark it as synced in the local database
                dao.markAsSynced(snap.id)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry() // Tells WorkManager to try again later if the network drops
        }
    }
}