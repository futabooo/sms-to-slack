package com.futabooo.smstoslack.domain.model

data class SmsMessage(
    val sender: String,
    val body: String,
    val timestamp: Long
)
