package com.futabooo.smstoslack.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.futabooo.smstoslack.worker.SlackPostWorker

class SmsBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SmsBroadcastReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        val smsMessage = SmsParser.parse(intent)
        if (smsMessage == null) {
            Log.w(TAG, "Failed to parse SMS from intent")
            return
        }

        Log.d(TAG, "SMS received from: ${smsMessage.sender}")

        val inputData = Data.Builder()
            .putString(SlackPostWorker.KEY_SENDER, smsMessage.sender)
            .putString(SlackPostWorker.KEY_BODY, smsMessage.body)
            .putLong(SlackPostWorker.KEY_TIMESTAMP, smsMessage.timestamp)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<SlackPostWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
