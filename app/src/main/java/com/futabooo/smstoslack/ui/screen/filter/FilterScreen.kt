package com.futabooo.smstoslack.ui.screen.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futabooo.smstoslack.data.local.entity.FilterRuleEntity
import com.futabooo.smstoslack.di.AppContainer
import com.futabooo.smstoslack.domain.model.FilterRuleType

@Composable
fun FilterScreen(appContainer: AppContainer) {
    val viewModel: FilterViewModel = viewModel(
        factory = FilterViewModel.factory(appContainer)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "ルール追加")
            }
        }
    ) { innerPadding ->
        if (uiState.rules.isEmpty()) {
            EmptyState(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.rules, key = { it.id }) { rule ->
                    FilterRuleItem(
                        rule = rule,
                        onToggleEnabled = { viewModel.toggleRuleEnabled(rule.id, it) },
                        onDelete = { viewModel.showDeleteConfirmation(rule.id) }
                    )
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddRuleDialog(
            onDismiss = { viewModel.dismissAddDialog() },
            onConfirm = { type, pattern -> viewModel.addRule(type, pattern) }
        )
    }

    uiState.showDeleteConfirmation?.let { ruleId ->
        DeleteConfirmationDialog(
            onDismiss = { viewModel.dismissDeleteConfirmation() },
            onConfirm = { viewModel.deleteRule(ruleId) }
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "フィルタルールが登録されていません",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "右下の＋ボタンからルールを追加してください",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilterRuleItem(
    rule: FilterRuleEntity,
    onToggleEnabled: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (rule.type) {
                        FilterRuleType.SENDER -> "送信元"
                        FilterRuleType.KEYWORD -> "キーワード"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = rule.pattern,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Switch(
                checked = rule.enabled,
                onCheckedChange = onToggleEnabled
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "削除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AddRuleDialog(
    onDismiss: () -> Unit,
    onConfirm: (FilterRuleType, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(FilterRuleType.SENDER) }
    var pattern by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("フィルタルール追加") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("ルール種別", style = MaterialTheme.typography.labelLarge)
                Column(modifier = Modifier.selectableGroup()) {
                    FilterRuleType.entries.forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedType == type,
                                    onClick = { selectedType = type },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedType == type,
                                onClick = null
                            )
                            Text(
                                text = when (type) {
                                    FilterRuleType.SENDER -> "送信元番号"
                                    FilterRuleType.KEYWORD -> "キーワード"
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            when (selectedType) {
                                FilterRuleType.SENDER -> "電話番号パターン"
                                FilterRuleType.KEYWORD -> "キーワード"
                            }
                        )
                    },
                    isError = pattern.isNotEmpty() && pattern.isBlank(),
                    supportingText = if (pattern.isNotEmpty() && pattern.isBlank()) {
                        { Text("パターンを入力してください") }
                    } else {
                        null
                    },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedType, pattern) },
                enabled = pattern.isNotBlank()
            ) {
                Text("追加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}

@Composable
private fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ルール削除") },
        text = { Text("このフィルタルールを削除しますか？") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("削除", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}
