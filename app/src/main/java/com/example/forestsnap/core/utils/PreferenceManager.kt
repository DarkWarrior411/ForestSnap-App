package com.example.forestsnap.core.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore by preferencesDataStore(name = "settings")

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val appContext = context.applicationContext

    companion object {
        val THEME_KEY = stringPreferencesKey("app_theme")
        val COMPRESSION_KEY = booleanPreferencesKey("local_compression")
        val STRICT_LOCATION_KEY = booleanPreferencesKey("strict_location")
        val OFFLINE_MODE_KEY = booleanPreferencesKey("offline_mode")
    }

    val themeFlow: Flow<String> = appContext.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "System Default"
    }

    val compressionFlow: Flow<Boolean> = appContext.dataStore.data.map { preferences ->
        preferences[COMPRESSION_KEY] ?: true
    }

    val strictLocationFlow: Flow<Boolean> = appContext.dataStore.data.map { preferences ->
        preferences[STRICT_LOCATION_KEY] ?: true
    }

    val offlineModeFlow: Flow<Boolean> = appContext.dataStore.data.map { preferences ->
        preferences[OFFLINE_MODE_KEY] ?: false
    }

    suspend fun saveTheme(theme: String) {
        appContext.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }

    suspend fun setCompression(enabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[COMPRESSION_KEY] = enabled
        }
    }

    suspend fun setStrictLocation(enabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[STRICT_LOCATION_KEY] = enabled
        }
    }

    suspend fun setOfflineMode(enabled: Boolean) {
        appContext.dataStore.edit { preferences ->
            preferences[OFFLINE_MODE_KEY] = enabled
        }
    }
}