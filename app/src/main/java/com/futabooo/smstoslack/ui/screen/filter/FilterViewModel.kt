package com.futabooo.smstoslack.ui.screen.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.futabooo.smstoslack.data.local.entity.FilterRuleEntity
import com.futabooo.smstoslack.data.repository.FilterRepository
import com.futabooo.smstoslack.di.AppContainer
import com.futabooo.smstoslack.domain.model.FilterRuleType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FilterUiState(
    val rules: List<FilterRuleEntity> = emptyList(),
    val showAddDialog: Boolean = false,
    val showDeleteConfirmation: Long? = null
)

class FilterViewModel(
    private val filterRepository: FilterRepository
) : ViewModel() {

    private val showAddDialog = MutableStateFlow(false)
    private val showDeleteConfirmation = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<FilterUiState> = combine(
        filterRepository.allRulesFlow,
        showAddDialog,
        showDeleteConfirmation
    ) { rules, addDialog, deleteConfirm ->
        FilterUiState(
            rules = rules,
            showAddDialog = addDialog,
            showDeleteConfirmation = deleteConfirm
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FilterUiState()
    )

    fun showAddDialog() {
        showAddDialog.value = true
    }

    fun dismissAddDialog() {
        showAddDialog.value = false
    }

    fun addRule(type: FilterRuleType, pattern: String) {
        if (pattern.isBlank()) return
        viewModelScope.launch {
            filterRepository.addRule(type, pattern.trim())
            showAddDialog.value = false
        }
    }

    fun showDeleteConfirmation(id: Long) {
        showDeleteConfirmation.value = id
    }

    fun dismissDeleteConfirmation() {
        showDeleteConfirmation.value = null
    }

    fun deleteRule(id: Long) {
        viewModelScope.launch {
            filterRepository.deleteRule(id)
            showDeleteConfirmation.value = null
        }
    }

    fun toggleRuleEnabled(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            filterRepository.updateRuleEnabled(id, enabled)
        }
    }

    companion object {
        fun factory(appContainer: AppContainer): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FilterViewModel(
                        filterRepository = appContainer.filterRepository
                    ) as T
                }
            }
        }
    }
}
