package com.futabooo.smstoslack.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futabooo.smstoslack.data.local.entity.ForwardedMessageEntity
import com.futabooo.smstoslack.data.repository.ForwardedMessageRepository
import com.futabooo.smstoslack.data.repository.SettingsRepository
import com.futabooo.smstoslack.di.AppContainer
import com.futabooo.smstoslack.domain.model.FilterMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val forwardingEnabled: Boolean = false,
    val filterMode: FilterMode = FilterMode.DISABLED,
    val webhookConfigured: Boolean = false,
    val recentMessages: List<ForwardedMessageEntity> = emptyList()
)

class DashboardViewModel(
    private val settingsRepository: SettingsRepository,
    private val forwardedMessageRepository: ForwardedMessageRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        settingsRepository.forwardingEnabledFlow,
        settingsRepository.filterModeFlow,
        settingsRepository.webhookUrlFlow,
        forwardedMessageRepository.getRecentMessages()
    ) { forwardingEnabled, filterMode, webhookUrl, messages ->
        DashboardUiState(
            forwardingEnabled = forwardingEnabled,
            filterMode = filterMode,
            webhookConfigured = !webhookUrl.isNullOrBlank(),
            recentMessages = messages.take(5)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )

    fun toggleForwarding(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveForwardingEnabled(enabled)
        }
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DashboardViewModel(
                        settingsRepository = appContainer.settingsRepository,
                        forwardedMessageRepository = appContainer.forwardedMessageRepository
                    ) as T
                }
            }
        }
    }
}
