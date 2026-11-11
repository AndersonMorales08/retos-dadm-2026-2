package com.example.interesmap

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private val radiusKey = floatPreferencesKey("radius_km")

    val radiusKm: Flow<Float> = context.dataStore.data.map { it[radiusKey] ?: 2f }

    suspend fun setRadiusKm(value: Float) {
        context.dataStore.edit { it[radiusKey] = value }
    }
}