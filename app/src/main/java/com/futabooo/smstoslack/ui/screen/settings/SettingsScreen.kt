package com.futabooo.smstoslack.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futabooo.smstoslack.di.AppContainer
import com.futabooo.smstoslack.domain.model.FilterMode

@Composable
fun SettingsScreen(appContainer: AppContainer) {
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.factory(appContainer)
    )
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WebhookUrlCard(
            url = uiState.webhookUrl,
            isValid = uiState.isWebhookUrlValid,
            onUrlChange = { viewModel.updateWebhookUrl(it) },
            testSendStatus = uiState.testSendStatus,
            onTestSend = { viewModel.sendTestMessage() }
        )

        FilterModeCard(
            filterMode = uiState.filterMode,
            onFilterModeChange = { viewModel.updateFilterMode(it) }
        )
    }
}

@Composable
private fun WebhookUrlCard(
    url: String,
    isValid: Boolean,
    onUrlChange: (String) -> Unit,
    testSendStatus: TestSendStatus,
    onTestSend: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Slack Webhook URL",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = url,
                onValueChange = onUrlChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Webhook URL") },
                placeholder = { Text("https://hooks.slack.com/services/...") },
                isError = url.isNotEmpty() && !isValid,
                supportingText = if (url.isNotEmpty() && !isValid) {
                    { Text("有効な HTTPS URL を入力してください") }
                } else {
                    null
                },
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onTestSend,
                    enabled = isValid && testSendStatus !is TestSendStatus.Sending
                ) {
                    if (testSendStatus is TestSendStatus.Sending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("テスト送信")
                    }
                }

                when (testSendStatus) {
                    is TestSendStatus.Success -> Text(
                        text = "送信成功",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    is TestSendStatus.Failed -> Text(
                        text = "送信失敗: ${testSendStatus.errorMessage}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun FilterModeCard(
    filterMode: FilterMode,
    onFilterModeChange: (FilterMode) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "フィルタモード",
                style = MaterialTheme.typography.titleMedium
            )

            Column(modifier = Modifier.selectableGroup()) {
                FilterMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = filterMode == mode,
                                onClick = { onFilterModeChange(mode) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = filterMode == mode,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = when (mode) {
                                    FilterMode.DISABLED -> "無効"
                                    FilterMode.WHITELIST -> "ホワイトリスト"
                                    FilterMode.BLACKLIST -> "ブラックリスト"
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = when (mode) {
                                    FilterMode.DISABLED -> "全てのSMSを転送"
                                    FilterMode.WHITELIST -> "ルールに一致するSMSのみ転送"
                                    FilterMode.BLACKLIST -> "ルールに一致するSMSを除外"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
