package com.futabooo.smstoslack.data.remote

import com.futabooo.smstoslack.domain.model.SlackPayload
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import android.util.Log
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class SlackPostResult {
    data object Success : SlackPostResult()
    data class ClientError(val code: Int, val message: String) : SlackPostResult()
    data class RetryableError(val code: Int?, val message: String) : SlackPostResult()
}

class SlackWebhookApi(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
) {
    private val json = Json { encodeDefaults = true }
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun post(webhookUrl: String, payload: SlackPayload): SlackPostResult {
        val jsonBody = json.encodeToString(payload)
        val requestBody = jsonBody.toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(webhookUrl)
            .post(requestBody)
            .build()

        Log.d("SlackWebhookApi", "Sending payload: $jsonBody")
        return try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                Log.d("SlackWebhookApi", "Response: code=${response.code}, body=$responseBody")
                when {
                    response.isSuccessful && responseBody.trim() == "ok" -> SlackPostResult.Success
                    response.isSuccessful -> SlackPostResult.ClientError(
                        code = response.code,
                        message = "Webhook URLが無効です。Slackで再発行してください。"
                    )
                    response.code in 400..499 -> SlackPostResult.ClientError(
                        code = response.code,
                        message = responseBody.ifEmpty { "Client error" }
                    )
                    else -> SlackPostResult.RetryableError(
                        code = response.code,
                        message = responseBody.ifEmpty { "Server error" }
                    )
                }
            }
        } catch (e: IOException) {
            SlackPostResult.RetryableError(
                code = null,
                message = e.message ?: "Network error"
            )
        }
    }
}
