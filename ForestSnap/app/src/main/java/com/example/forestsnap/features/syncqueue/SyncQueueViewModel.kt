package com.example.forestsnap.features.syncqueue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.forestsnap.data.repository.SyncSnapRepository
import kotlinx.coroutines.flow.Flow

class SyncQueueViewModel(
    private val repository: SyncSnapRepository
) : ViewModel() {
    
    val unsyncedItems: Flow<List<com.example.forestsnap.data.local.SyncSnapEntity>> = repository.getUnsynced()
    val unsyncedCount: Flow<Int> = repository.getUnsyncedCount()
    
    suspend fun syncAll() {
        // Implement sync logic
    }
    
    suspend fun retryFailed() {
        // Implement retry logic
    }
    
    suspend fun clearSynced() {
        repository.deleteSynced()
    }
}

class SyncQueueViewModelFactory(
    private val repository: SyncSnapRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SyncQueueViewModel(repository) as T
    }
}
