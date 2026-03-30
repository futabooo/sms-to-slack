package com.futabooo.smstoslack.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.futabooo.smstoslack.data.local.AppDatabase
import com.futabooo.smstoslack.data.local.datastore.SettingsDataStore
import com.futabooo.smstoslack.data.remote.SlackPostResult
import com.futabooo.smstoslack.data.remote.SlackWebhookApi
import com.futabooo.smstoslack.data.repository.FilterRepository
import com.futabooo.smstoslack.data.repository.ForwardedMessageRepository
import com.futabooo.smstoslack.data.repository.SettingsRepository
import com.futabooo.smstoslack.domain.filter.SmsFilterEvaluator
import com.futabooo.smstoslack.domain.model.FilterMode
import com.futabooo.smstoslack.domain.model.ForwardingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SlackPostWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_SENDER = "sms_sender"
        const val KEY_BODY = "sms_body"
        const val KEY_TIMESTAMP = "sms_timestamp"
        private const val TAG = "SlackPostWorker"
    }

    private val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(SettingsDataStore(applicationContext))
    }

    private val slackWebhookApi: SlackWebhookApi by lazy {
        SlackWebhookApi()
    }

    private val forwardedMessageRepository: ForwardedMessageRepository by lazy {
        ForwardedMessageRepository(
            AppDatabase.getInstance(applicationContext).forwardedMessageDao()
        )
    }

    private val filterRepository: FilterRepository by lazy {
        FilterRepository(
            AppDatabase.getInstance(applicationContext).filterRuleDao()
        )
    }

    override suspend fun doWork(): Result {
        val sender = inputData.getString(KEY_SENDER) ?: return Result.failure()
        val body = inputData.getString(KEY_BODY) ?: return Result.failure()
        val timestamp = inputData.getLong(KEY_TIMESTAMP, 0L)

        val forwardingEnabled = settingsRepository.isForwardingEnabled()
        if (!forwardingEnabled) {
            Log.d(TAG, "Forwarding disabled, skipping SMS from $sender")
            forwardedMessageRepository.save(sender, body, timestamp, ForwardingStatus.FILTERED, null)
            return Result.success()
        }

        // Filter evaluation
        val filterMode = try {
            settingsRepository.getFilterMode()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get filter mode, defaulting to DISABLED", e)
            FilterMode.DISABLED
        }

        if (filterMode != FilterMode.DISABLED) {
            val rules = try {
                filterRepository.getEnabledRules()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to get filter rules, skipping filter", e)
                emptyList()
            }

            val shouldForward = SmsFilterEvaluator.shouldForward(sender, body, filterMode, rules)
            if (!shouldForward) {
                Log.d(TAG, "SMS from $sender filtered out by $filterMode rules")
                forwardedMessageRepository.save(sender, body, timestamp, ForwardingStatus.FILTERED, null)
                return Result.success()
            }
        }

        val webhookUrl = settingsRepository.getWebhookUrl()
        if (webhookUrl.isNullOrBlank()) {
            Log.w(TAG, "Webhook URL not configured")
            return Result.failure()
        }

        val payload = SlackMessageFormatter.format(sender, body, timestamp)

        return withContext(Dispatchers.IO) {
            when (val result = slackWebhookApi.post(webhookUrl, payload)) {
                is SlackPostResult.Success -> {
                    Log.d(TAG, "Successfully posted SMS from $sender to Slack")
                    forwardedMessageRepository.save(sender, body, timestamp, ForwardingStatus.SUCCESS, 200)
                    Result.success()
                }
                is SlackPostResult.ClientError -> {
                    Log.e(TAG, "Client error posting to Slack: ${result.code} ${result.message}")
                    forwardedMessageRepository.save(sender, body, timestamp, ForwardingStatus.FAILED, result.code)
                    Result.failure()
                }
                is SlackPostResult.RetryableError -> {
                    Log.w(TAG, "Retryable error posting to Slack: ${result.code} ${result.message}")
                    Result.retry()
                }
            }
        }
    }
}
