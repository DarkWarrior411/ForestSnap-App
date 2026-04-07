package com.example.forestsnap.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncSnapDao {
    
    // Inserts a new captured photo into the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnap(snap: SyncSnapEntity)

    // Gets a live stream of all pending uploads. 
    // Using Flow means the UI will update automatically when a new photo is taken!
    @Query("SELECT * FROM sync_queue WHERE isSynced = 0 ORDER BY id DESC")
    fun getPendingSnaps(): Flow<List<SyncSnapEntity>>

    // Marks a specific photo as synced once AWS confirms receipt
    @Query("UPDATE sync_queue SET isSynced = 1 WHERE id = :snapId")
    suspend fun markAsSynced(snapId: Int)

    // Deletes all synced snaps to free up storage space on the device
    @Query("DELETE FROM sync_queue WHERE isSynced = 1")
    suspend fun clearSyncedSnaps()
}