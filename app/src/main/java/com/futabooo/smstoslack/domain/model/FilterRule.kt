package com.futabooo.smstoslack.domain.model

data class FilterRule(
    val id: Long,
    val type: FilterRuleType,
    val pattern: String,
    val enabled: Boolean
)
