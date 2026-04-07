package com.example.forestsnap.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncSnapEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val imagePath: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: String,
    val isSynced: Boolean = false // false = pending upload, true = synced to AWS
)