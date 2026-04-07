package com.example.forestsnap.core.utils

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

// DataStore for storing user preferences
val Context.dataStore by preferencesDataStore(name = "forestsnap_preferences")

object PreferenceKeys {
    val USER_ID = stringPreferencesKey("user_id")
    val LAST_SYNC_TIME = stringPreferencesKey("last_sync_time")
    val SYNC_ENABLED = stringPreferencesKey("sync_enabled")
}
