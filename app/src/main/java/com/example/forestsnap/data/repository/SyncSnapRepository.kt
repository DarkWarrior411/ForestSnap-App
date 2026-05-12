package com.example.forestsnap.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.forestsnap.data.local.ForestDatabase
import com.example.forestsnap.data.local.SyncSnapEntity
import com.example.forestsnap.data.sync.CloudSyncWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import com.example.forestsnap.core.utils.PreferenceManager

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncSnapRepository @Inject constructor(
    database: ForestDatabase, 
    private val context: Context,
    private val preferenceManager: PreferenceManager
) {
    private val syncSnapDao = database.syncSnapDao()

    suspend fun insertSyncSnap(syncSnap: SyncSnapEntity) {
        syncSnapDao.insertSnap(syncSnap)

        val isOfflineMode = preferenceManager.offlineModeFlow.first()
        if (!isOfflineMode) {
            enqueueAutoSync()
        }
    }

    private fun enqueueAutoSync() {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<CloudSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "AutoCloudSync",
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            syncWorkRequest
        )
    }

    fun getUnsynced(): Flow<List<SyncSnapEntity>> {
        return syncSnapDao.getPendingSnaps()
    }

    fun getUnsyncedCount(): Flow<Int> {
        return syncSnapDao.getUnsyncedCount()
    }

    suspend fun deleteSynced() {
        syncSnapDao.clearSyncedSnaps()
    }
}