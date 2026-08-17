package com.example.forestsnap.features.syncqueue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forestsnap.data.repository.SyncSnapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel holding StateFlow of pending offline snapshots from SyncSnapRepository.
 */
@HiltViewModel
class SyncQueueViewModel @Inject constructor(
    repository: SyncSnapRepository
) : ViewModel() {
    val pendingQueue = repository.getUnsynced()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}