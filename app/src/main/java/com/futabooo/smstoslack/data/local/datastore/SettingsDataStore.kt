package com.futabooo.smstoslack.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.futabooo.smstoslack.domain.model.FilterMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    private companion object {
        val WEBHOOK_URL = stringPreferencesKey("webhook_url")
        val FORWARDING_ENABLED = booleanPreferencesKey("forwarding_enabled")
        val FILTER_MODE = stringPreferencesKey("filter_mode")
    }

    val webhookUrlFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[WEBHOOK_URL]
    }

    val forwardingEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[FORWARDING_ENABLED] ?: true
    }

    suspend fun saveWebhookUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[WEBHOOK_URL] = url
        }
    }

    suspend fun saveForwardingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[FORWARDING_ENABLED] = enabled
        }
    }

    val filterModeFlow: Flow<FilterMode> = context.dataStore.data.map { preferences ->
        val name = preferences[FILTER_MODE]
        if (name != null) {
            try {
                FilterMode.valueOf(name)
            } catch (_: IllegalArgumentException) {
                FilterMode.DISABLED
            }
        } else {
            FilterMode.DISABLED
        }
    }

    suspend fun saveFilterMode(mode: FilterMode) {
        context.dataStore.edit { preferences ->
            preferences[FILTER_MODE] = mode.name
        }
    }
}
