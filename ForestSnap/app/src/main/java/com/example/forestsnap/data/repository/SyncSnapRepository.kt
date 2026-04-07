package com.example.forestsnap.data.repository

import com.example.forestsnap.data.local.ForestDatabase
import com.example.forestsnap.data.local.SyncSnapEntity
import kotlinx.coroutines.flow.Flow

class SyncSnapRepository(database: ForestDatabase) {
    private val syncSnapDao = database.syncSnapDao()

    suspend fun insertSyncSnap(syncSnap: SyncSnapEntity): Long {
        return syncSnapDao.insert(syncSnap)
    }

    suspend fun updateSyncSnap(syncSnap: SyncSnapEntity) {
        syncSnapDao.update(syncSnap)
    }

    suspend fun deleteSyncSnap(syncSnap: SyncSnapEntity) {
        syncSnapDao.delete(syncSnap)
    }

    suspend fun getSyncSnapById(id: Int): SyncSnapEntity? {
        return syncSnapDao.getById(id)
    }

    fun getAllSyncSnaps(): Flow<List<SyncSnapEntity>> {
        return syncSnapDao.getAllAsFlow()
    }

    fun getUnsynced(): Flow<List<SyncSnapEntity>> {
        return syncSnapDao.getUnsyncedAsFlow()
    }

    fun getUnsyncedCount(): Flow<Int> {
        return syncSnapDao.getUnsyncedCount()
    }

    suspend fun deleteSynced() {
        syncSnapDao.deleteSynced()
    }

    suspend fun deleteAll() {
        syncSnapDao.deleteAll()
    }
}
