package com.futabooo.smstoslack.ui.permission

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.futabooo.smstoslack.domain.model.SmsPermissionStatus

@Composable
fun SmsPermissionHandler(
    onPermissionStatusChanged: (SmsPermissionStatus) -> Unit,
    content: @Composable (SmsPermissionStatus) -> Unit
) {
    val context = LocalContext.current
    var permissionStatus by remember { mutableStateOf(SmsPermissionStatus.DENIED) }
    var showRationale by remember { mutableStateOf(false) }
    var hasRequestedOnce by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionStatus = if (isGranted) {
            SmsPermissionStatus.GRANTED
        } else {
            hasRequestedOnce = true
            SmsPermissionStatus.DENIED
        }
        onPermissionStatusChanged(permissionStatus)
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED

        permissionStatus = if (granted) {
            SmsPermissionStatus.GRANTED
        } else {
            SmsPermissionStatus.DENIED
        }
        onPermissionStatusChanged(permissionStatus)
    }

    if (showRationale) {
        SmsPermissionRationaleDialog(
            onConfirm = {
                showRationale = false
                permissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
            },
            onDismiss = { showRationale = false }
        )
    }

    Column {
        if (permissionStatus != SmsPermissionStatus.GRANTED) {
            SmsPermissionCard(
                permissionStatus = permissionStatus,
                hasRequestedOnce = hasRequestedOnce,
                onRequestPermission = {
                    showRationale = true
                },
                onOpenSettings = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        content(permissionStatus)
    }
}

@Composable
private fun SmsPermissionCard(
    permissionStatus: SmsPermissionStatus,
    hasRequestedOnce: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "SMS受信パーミッションが必要です",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = "SMSをSlackに転送するために、SMS受信パーミッションを許可してください。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            if (hasRequestedOnce && permissionStatus == SmsPermissionStatus.DENIED) {
                Button(onClick = onOpenSettings) {
                    Text("設定画面を開く")
                }
            } else {
                Button(onClick = onRequestPermission) {
                    Text("パーミッションを許可する")
                }
            }
        }
    }
}

@Composable
private fun SmsPermissionRationaleDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("SMS受信パーミッション") },
        text = {
            Text("このアプリはSMSを受信してSlackに転送します。SMS受信パーミッションを許可すると、新着SMSを自動的に検知できるようになります。")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("許可する")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル")
            }
        }
    )
}
