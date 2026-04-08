package com.example.forestsnap.data.repository

import com.example.forestsnap.data.local.ForestDatabase
import com.example.forestsnap.data.local.SyncSnapEntity
import kotlinx.coroutines.flow.Flow

class SyncSnapRepository(database: ForestDatabase) {
    private val syncSnapDao = database.syncSnapDao()

    suspend fun insertSyncSnap(syncSnap: SyncSnapEntity) {
        syncSnapDao.insertSnap(syncSnap)
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
