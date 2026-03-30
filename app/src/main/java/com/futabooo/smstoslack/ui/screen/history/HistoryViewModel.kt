package com.futabooo.smstoslack.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futabooo.smstoslack.data.local.entity.ForwardedMessageEntity
import com.futabooo.smstoslack.data.repository.ForwardedMessageRepository
import com.futabooo.smstoslack.di.AppContainer
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HistoryUiState(
    val messages: List<ForwardedMessageEntity> = emptyList()
)

class HistoryViewModel(
    forwardedMessageRepository: ForwardedMessageRepository
) : ViewModel() {

    val uiState: StateFlow<HistoryUiState> = forwardedMessageRepository.getRecentMessages()
        .map { messages -> HistoryUiState(messages = messages) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryUiState()
        )

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HistoryViewModel(
                        forwardedMessageRepository = appContainer.forwardedMessageRepository
                    ) as T
                }
            }
        }
    }
}
