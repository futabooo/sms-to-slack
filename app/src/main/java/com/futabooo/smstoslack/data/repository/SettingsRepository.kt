package com.futabooo.smstoslack.data.repository

import com.futabooo.smstoslack.data.local.datastore.SettingsDataStore
import com.futabooo.smstoslack.domain.model.FilterMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SettingsRepository(private val dataStore: SettingsDataStore) {

    val webhookUrlFlow: Flow<String?> = dataStore.webhookUrlFlow

    val forwardingEnabledFlow: Flow<Boolean> = dataStore.forwardingEnabledFlow

    suspend fun saveWebhookUrl(url: String) {
        dataStore.saveWebhookUrl(url)
    }

    suspend fun saveForwardingEnabled(enabled: Boolean) {
        dataStore.saveForwardingEnabled(enabled)
    }

    suspend fun getWebhookUrl(): String? {
        return dataStore.webhookUrlFlow.first()
    }

    suspend fun isForwardingEnabled(): Boolean {
        return dataStore.forwardingEnabledFlow.first()
    }

    val filterModeFlow: Flow<FilterMode> = dataStore.filterModeFlow

    suspend fun saveFilterMode(mode: FilterMode) {
        dataStore.saveFilterMode(mode)
    }

    suspend fun getFilterMode(): FilterMode {
        return dataStore.filterModeFlow.first()
    }

    fun isValidWebhookUrl(url: String): Boolean {
        if (url.isBlank()) return false
        return try {
            val trimmed = url.trim()
            trimmed.startsWith("https://hooks.slack.com/services/") &&
                android.util.Patterns.WEB_URL.matcher(trimmed).matches()
        } catch (_: Exception) {
            false
        }
    }
}
