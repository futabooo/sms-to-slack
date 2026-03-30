package com.futabooo.smstoslack.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futabooo.smstoslack.data.remote.SlackPostResult
import com.futabooo.smstoslack.data.remote.SlackWebhookApi
import com.futabooo.smstoslack.data.repository.SettingsRepository
import com.futabooo.smstoslack.di.AppContainer
import com.futabooo.smstoslack.domain.model.FilterMode
import com.futabooo.smstoslack.domain.model.SlackPayload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class TestSendStatus {
    data object Idle : TestSendStatus()
    data object Sending : TestSendStatus()
    data object Success : TestSendStatus()
    data class Failed(val errorMessage: String) : TestSendStatus()
}

data class SettingsUiState(
    val webhookUrl: String = "",
    val isWebhookUrlValid: Boolean = false,
    val filterMode: FilterMode = FilterMode.DISABLED,
    val testSendStatus: TestSendStatus = TestSendStatus.Idle
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val slackWebhookApi: SlackWebhookApi
) : ViewModel() {

    private val webhookUrlInput = MutableStateFlow("")
    private val testSendStatus = MutableStateFlow<TestSendStatus>(TestSendStatus.Idle)
    private var initialized = false

    val uiState: StateFlow<SettingsUiState> = combine(
        webhookUrlInput,
        settingsRepository.filterModeFlow,
        testSendStatus
    ) { url, filterMode, sendStatus ->
        SettingsUiState(
            webhookUrl = url,
            isWebhookUrlValid = settingsRepository.isValidWebhookUrl(url),
            filterMode = filterMode,
            testSendStatus = sendStatus
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    init {
        viewModelScope.launch {
            settingsRepository.webhookUrlFlow.collect { url ->
                if (!initialized) {
                    webhookUrlInput.value = url ?: ""
                    initialized = true
                }
            }
        }
    }

    fun updateWebhookUrl(url: String) {
        webhookUrlInput.value = url
        if (settingsRepository.isValidWebhookUrl(url)) {
            viewModelScope.launch {
                settingsRepository.saveWebhookUrl(url)
            }
        }
    }

    fun updateFilterMode(mode: FilterMode) {
        viewModelScope.launch {
            settingsRepository.saveFilterMode(mode)
        }
    }

    fun sendTestMessage() {
        val url = webhookUrlInput.value
        if (!settingsRepository.isValidWebhookUrl(url)) return

        testSendStatus.value = TestSendStatus.Sending
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                slackWebhookApi.post(url, SlackPayload(text = "SMS to Slack テスト送信"))
            }
            testSendStatus.value = when (result) {
                is SlackPostResult.Success -> TestSendStatus.Success
                is SlackPostResult.ClientError -> TestSendStatus.Failed(
                    "HTTP ${result.code}: ${result.message}"
                )
                is SlackPostResult.RetryableError -> TestSendStatus.Failed(
                    if (result.code != null) "HTTP ${result.code}: ${result.message}"
                    else result.message
                )
            }
        }
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(
                        settingsRepository = appContainer.settingsRepository,
                        slackWebhookApi = appContainer.slackWebhookApi
                    ) as T
                }
            }
        }
    }
}
