// app/src/main/java/com/example/forestsnap/features/syncqueue/SyncQueueViewModel.kt

package com.example.forestsnap.features.syncqueue

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.forestsnap.data.local.ForestDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class SyncQueueViewModel(application: Application) : AndroidViewModel(application) {
    private val db = ForestDatabase.getDatabase(application)
    private val dao = db.syncSnapDao()

    val pendingQueue = dao.getPendingSnaps()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}