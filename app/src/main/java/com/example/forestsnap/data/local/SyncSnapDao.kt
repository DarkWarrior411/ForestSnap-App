package com.example.forestsnap.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncSnapDao {
    @Insert
    suspend fun insertSnap(snap: SyncSnapEntity)

    @Query("SELECT * FROM sync_snaps WHERE isSynced = 0 AND isSyncing = 0 ORDER BY timestamp DESC")
    fun getPendingSnaps(): Flow<List<SyncSnapEntity>>

    @Query("UPDATE sync_snaps SET isSyncing = :isSyncing WHERE id = :snapId")
    suspend fun setSyncingStatus(snapId: Int, isSyncing: Boolean)

    @Query("UPDATE sync_snaps SET isSynced = 1, isSyncing = 0 WHERE id = :snapId")
    suspend fun markAsSynced(snapId: Int)

    @Query("UPDATE sync_snaps SET isSynced = 1, isSyncing = 0, fireRiskPercent = :fireRisk, fuelLoadScore = :fuelLoad, drynessTier = :dryness WHERE id = :snapId")
    suspend fun updateAnalysisAndMarkSynced(snapId: Int, fireRisk: Double, fuelLoad: Double, dryness: Int)

    @Query("SELECT COUNT(*) FROM sync_snaps WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>

    @Query("SELECT * FROM sync_snaps WHERE isSynced = 1 ORDER BY timestamp DESC LIMIT 1")
    fun getLatestSyncedSnap(): Flow<SyncSnapEntity?>

    @Query("DELETE FROM sync_snaps WHERE isSynced = 1")
    suspend fun clearSyncedSnaps()

    @Query("DELETE FROM sync_snaps WHERE id = :snapId")
    suspend fun deleteSnap(snapId: Int)
}