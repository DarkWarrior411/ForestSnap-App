package com.example.forestsnap.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.forestsnap.core.utils.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    val themeFlow = preferenceManager.themeFlow
    val compressionFlow = preferenceManager.compressionFlow
    val strictLocationFlow = preferenceManager.strictLocationFlow
    val offlineModeFlow = preferenceManager.offlineModeFlow

    fun updateTheme(theme: String) {
        viewModelScope.launch { preferenceManager.saveTheme(theme) }
    }

    fun updateCompression(enabled: Boolean) {
        viewModelScope.launch { preferenceManager.setCompression(enabled) }
    }

    fun updateStrictLocation(enabled: Boolean) {
        viewModelScope.launch { preferenceManager.setStrictLocation(enabled) }
    }

    fun updateOfflineMode(enabled: Boolean) {
        viewModelScope.launch { preferenceManager.setOfflineMode(enabled) }
    }
}
