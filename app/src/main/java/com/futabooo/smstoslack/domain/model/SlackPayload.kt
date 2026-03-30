package com.futabooo.smstoslack.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SlackPayload(
    val text: String
)
