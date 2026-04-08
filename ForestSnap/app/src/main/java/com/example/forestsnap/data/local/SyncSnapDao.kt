// app/src/main/java/com/example/forestsnap/data/local/SyncSnapDao.kt

package com.example.forestsnap.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncSnapDao {
    @Insert
    suspend fun insertSnap(snap: SyncSnapEntity)

    @Query("SELECT * FROM sync_snaps WHERE isSynced = 0 ORDER BY timestamp DESC")
    fun getPendingSnaps(): Flow<List<SyncSnapEntity>>

    @Query("UPDATE sync_snaps SET isSynced = 1 WHERE id = :snapId")
    suspend fun markAsSynced(snapId: Int)

    @Query("SELECT COUNT(*) FROM sync_snaps WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>

    @Query("DELETE FROM sync_snaps WHERE isSynced = 1")
    suspend fun clearSyncedSnaps()
}