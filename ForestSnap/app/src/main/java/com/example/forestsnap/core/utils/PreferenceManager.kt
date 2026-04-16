package com.example.forestsnap.core.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore for storing user preferences
val Context.dataStore by preferencesDataStore(name = "forestsnap_preferences")

object PreferenceKeys {
    val USER_ID = stringPreferencesKey("user_id")
    val LAST_SYNC_TIME = stringPreferencesKey("last_sync_time")
    val SYNC_ENABLED = stringPreferencesKey("sync_enabled")
    val THEME_MODE = stringPreferencesKey("theme_mode")
}

class PreferenceManager(private val context: Context) {

    val themeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferenceKeys.THEME_MODE] ?: "System Default"
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.THEME_MODE] = mode
        }
    }
}
