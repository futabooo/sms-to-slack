package com.futabooo.smstoslack.worker

import com.futabooo.smstoslack.domain.model.SlackPayload
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object SlackMessageFormatter {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")

    fun format(sender: String, body: String, timestamp: Long): SlackPayload {
        val localDateTime = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        val formattedTime = dateFormatter.format(localDateTime)

        val text = buildString {
            appendLine("*$sender* | $formattedTime")
            body.lines().forEach { line ->
                appendLine(">$line")
            }
        }.trimEnd()

        return SlackPayload(text = text)
    }
}
