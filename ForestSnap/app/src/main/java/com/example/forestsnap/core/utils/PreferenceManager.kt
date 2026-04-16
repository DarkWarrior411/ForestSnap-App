package com.example.forestsnap.core.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// CRITICAL: This MUST sit outside the class at the top level to act as a true Singleton
val Context.dataStore by preferencesDataStore(name = "settings")

class PreferenceManager(context: Context) {
    // Force the use of the Application Context so all screens share the same DataStore
    private val appContext = context.applicationContext

    companion object {
        val THEME_KEY = stringPreferencesKey("app_theme")
    }

    // Read the theme from DataStore
    val themeFlow: Flow<String> = appContext.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "System Default"
    }

    // Save the theme to DataStore
    suspend fun saveTheme(theme: String) {
        appContext.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }
}