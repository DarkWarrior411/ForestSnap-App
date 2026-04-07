package com.example.forestsnap.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.forestsnap.data.repository.SyncSnapRepository

class DashboardViewModel(
    private val repository: SyncSnapRepository
) : ViewModel() {
    
    // Add ViewModel logic here
    fun refreshData() {
        // Refresh dashboard data
    }
}

class DashboardViewModelFactory(
    private val repository: SyncSnapRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DashboardViewModel(repository) as T
    }
}
