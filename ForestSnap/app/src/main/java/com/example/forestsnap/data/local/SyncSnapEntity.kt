// app/src/main/java/com/example/forestsnap/data/local/SyncSnapEntity.kt
package com.example.forestsnap.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_snaps")
data class SyncSnapEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val photoPath: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val isSynced: Boolean = false,
    
    // --- ADD THE AI RESULTS ---
    val fireRiskPercent: Double? = null,
    val fuelLoadScore: Double? = null,
    val drynessTier: Int? = null
)