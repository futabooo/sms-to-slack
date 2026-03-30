package com.futabooo.smstoslack.ui.screen.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.futabooo.smstoslack.data.local.entity.ForwardedMessageEntity
import com.futabooo.smstoslack.di.AppContainer
import com.futabooo.smstoslack.domain.model.FilterMode
import com.futabooo.smstoslack.domain.model.ForwardingStatus
import com.futabooo.smstoslack.ui.permission.SmsPermissionHandler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(appContainer: AppContainer) {
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.factory(appContainer)
    )
    val uiState by viewModel.uiState.collectAsState()

    SmsPermissionHandler(
        onPermissionStatusChanged = { }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ForwardingToggleCard(
                enabled = uiState.forwardingEnabled,
                onToggle = { viewModel.toggleForwarding(it) }
            )

            StatusCard(
                filterMode = uiState.filterMode,
                webhookConfigured = uiState.webhookConfigured
            )

            if (uiState.recentMessages.isNotEmpty()) {
                RecentMessagesCard(messages = uiState.recentMessages)
            }
        }
    }
}

@Composable
private fun ForwardingToggleCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SMS転送",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (enabled) "有効" else "無効",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
private fun StatusCard(
    filterMode: FilterMode,
    webhookConfigured: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "設定状態",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Webhook URL", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = if (webhookConfigured) "設定済み" else "未設定",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (webhookConfigured) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("フィルタモード", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = when (filterMode) {
                        FilterMode.WHITELIST -> "ホワイトリスト"
                        FilterMode.BLACKLIST -> "ブラックリスト"
                        FilterMode.DISABLED -> "無効"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun RecentMessagesCard(messages: List<ForwardedMessageEntity>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "最近の転送",
                style = MaterialTheme.typography.titleMedium
            )

            messages.forEach { message ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = message.sender,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = message.body.take(30) + if (message.body.length > 30) "..." else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatTimestamp(message.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = when (message.status) {
                                ForwardingStatus.SUCCESS -> "成功"
                                ForwardingStatus.FAILED -> "失敗"
                                ForwardingStatus.FILTERED -> "フィルタ"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = when (message.status) {
                                ForwardingStatus.SUCCESS -> MaterialTheme.colorScheme.primary
                                ForwardingStatus.FAILED -> MaterialTheme.colorScheme.error
                                ForwardingStatus.FILTERED -> CardDefaults.cardColors().containerColor
                            }
                        )
                    }
                }
                if (message != messages.last()) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
