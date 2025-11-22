package com.example.elgoharymusic.data.repoImpl

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// Single DataStore instance - ONLY define this ONCE in your entire app
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")