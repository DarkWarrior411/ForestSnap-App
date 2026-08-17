package com.example.forestsnap.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Room database entity table representing a captured field snapshot and its analysis state. */
@Entity(tableName = "sync_snaps")
data class SyncSnapEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val photoPath: String,
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: Long,
    val isSynced: Boolean = false,
    val isSyncing: Boolean = false,
    val syncStatus: String? = null,
    val lastAttemptedAt: Long? = null,

    val fireRiskPercent: Double? = null,
    val fuelLoadScore: Double? = null,
    val drynessTier: Int? = null,

    val temperatureC: Double? = null,
    val humidityPercent: Int? = null,
    val windSpeedMs: Double? = null,
    val windDirectionDeg: Int? = null
)