package com.futabooo.smstoslack.di

import android.content.Context
import com.futabooo.smstoslack.data.local.AppDatabase
import com.futabooo.smstoslack.data.local.datastore.SettingsDataStore
import com.futabooo.smstoslack.data.remote.SlackWebhookApi
import com.futabooo.smstoslack.data.repository.FilterRepository
import com.futabooo.smstoslack.data.repository.ForwardedMessageRepository
import com.futabooo.smstoslack.data.repository.SettingsRepository

class AppContainer(context: Context) {

    private val database: AppDatabase by lazy {
        AppDatabase.getInstance(context)
    }

    private val settingsDataStore: SettingsDataStore by lazy {
        SettingsDataStore(context)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(settingsDataStore)
    }

    val filterRepository: FilterRepository by lazy {
        FilterRepository(database.filterRuleDao())
    }

    val forwardedMessageRepository: ForwardedMessageRepository by lazy {
        ForwardedMessageRepository(database.forwardedMessageDao())
    }

    val slackWebhookApi: SlackWebhookApi by lazy {
        SlackWebhookApi()
    }
}
