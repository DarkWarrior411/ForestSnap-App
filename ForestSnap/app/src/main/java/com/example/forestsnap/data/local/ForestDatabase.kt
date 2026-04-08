// app/src/main/java/com/example/forestsnap/data/local/ForestDatabase.kt

package com.example.forestsnap.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SyncSnapEntity::class], version = 1, exportSchema = false)
abstract class ForestDatabase : RoomDatabase() {
    abstract fun syncSnapDao(): SyncSnapDao

    companion object {
        @Volatile
        private var INSTANCE: ForestDatabase? = null

        fun getDatabase(context: Context): ForestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ForestDatabase::class.java,
                    "forest_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}