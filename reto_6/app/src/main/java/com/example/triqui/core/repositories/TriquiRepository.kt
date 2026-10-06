package com.example.triqui.core.repositories

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.triqui.features.triqui.ui.TriquiUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "triqui_settings")

class TriquiRepository(private val context: Context) {

    companion object {
        private val KEY_UI_STATE = stringPreferencesKey("triqui_ui_state")
        private val KEY_LEVEL = stringPreferencesKey("triqui_level")
    }

    // Flujo para leer el estado guardado del juego
    val uiStateFlow: Flow<TriquiUiState> = context.dataStore.data.map { preferences ->
        val jsonString = preferences[KEY_UI_STATE]
        if (jsonString != null) {
            runCatching { Json.decodeFromString<TriquiUiState>(jsonString) }.getOrDefault(TriquiUiState())
        } else {
            TriquiUiState()
        }
    }

    // Flujo para leer la dificultad guardada
    val levelFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_LEVEL] ?: "easy"
    }

    // Guardar el estado del tablero y marcadores
    suspend fun saveUiState(uiState: TriquiUiState) {
        val jsonString = Json.encodeToString(uiState)
        context.dataStore.edit { preferences ->
            preferences[KEY_UI_STATE] = jsonString
        }
    }

    // Guardar el nivel de dificultad
    suspend fun saveLevel(levelKey: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LEVEL] = levelKey
        }
    }
}