package com.example.forestsnap

import android.app.Application
import com.example.forestsnap.data.local.ForestDatabase
import com.example.forestsnap.data.repository.SyncSnapRepository

class ForestSnapApplication : Application() {
    
    val database by lazy { ForestDatabase.getDatabase(this) }
    val repository by lazy { SyncSnapRepository(database) }
}