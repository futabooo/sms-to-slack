package com.futabooo.smstoslack.receiver

import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.util.Log
import com.futabooo.smstoslack.domain.model.SmsMessage

object SmsParser {

    private const val TAG = "SmsParser"

    fun parse(intent: Intent): SmsMessage? {
        val bundle = intent.extras ?: run {
            Log.w(TAG, "Intent extras is null")
            return null
        }

        @Suppress("DEPRECATION")
        val pdus = bundle.get("pdus") as? Array<*> ?: run {
            Log.w(TAG, "No pdus found in intent extras")
            return null
        }

        if (pdus.isEmpty()) {
            Log.w(TAG, "PDU array is empty")
            return null
        }

        val format = bundle.getString("format")

        var sender: String? = null
        val bodyBuilder = StringBuilder()
        var timestamp = 0L

        for (pdu in pdus) {
            try {
                val smsMessage = android.telephony.SmsMessage.createFromPdu(
                    pdu as ByteArray,
                    format
                )
                if (sender == null) {
                    sender = smsMessage.displayOriginatingAddress
                    timestamp = smsMessage.timestampMillis
                }
                bodyBuilder.append(smsMessage.displayMessageBody ?: "")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse PDU", e)
            }
        }

        if (sender.isNullOrEmpty() || bodyBuilder.isEmpty()) {
            Log.w(TAG, "Failed to extract sender or body from PDUs")
            return null
        }

        return SmsMessage(
            sender = sender,
            body = bodyBuilder.toString(),
            timestamp = timestamp
        )
    }
}
