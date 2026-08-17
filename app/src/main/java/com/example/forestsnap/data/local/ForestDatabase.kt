package com.example.forestsnap.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room database instance storing offline survey snapshots and upload sync status queue.
 */
@Database(entities = [SyncSnapEntity::class], version = 2, exportSchema = false)
abstract class ForestDatabase : RoomDatabase() {
    abstract fun syncSnapDao(): SyncSnapDao

    companion object {
        @Volatile
        private var INSTANCE: ForestDatabase? = null

        /** Database migration script from version 1 to 2 adding extra telemetry columns. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE sync_snaps_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, photoPath TEXT NOT NULL, latitude REAL, longitude REAL, timestamp INTEGER NOT NULL, isSynced INTEGER NOT NULL, isSyncing INTEGER NOT NULL, syncStatus TEXT, lastAttemptedAt INTEGER, windDirectionDeg INTEGER, fireRiskPercent REAL, fuelLoadScore REAL, drynessTier INTEGER)")

                database.execSQL("INSERT INTO sync_snaps_new (id, photoPath, latitude, longitude, timestamp, isSynced, isSyncing, windDirectionDeg, fireRiskPercent, fuelLoadScore, drynessTier) SELECT id, photoPath, latitude, longitude, timestamp, isSynced, isSyncing, windDirectionDeg, fireRiskPercent, fuelLoadScore, drynessTier FROM sync_snaps")

                database.execSQL("DROP TABLE sync_snaps")
                database.execSQL("ALTER TABLE sync_snaps_new RENAME TO sync_snaps")
            }
        }

        fun getDatabase(context: Context): ForestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ForestDatabase::class.java,
                    "forest_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}