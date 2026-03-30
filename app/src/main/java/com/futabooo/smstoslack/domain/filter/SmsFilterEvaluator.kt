package com.futabooo.smstoslack.domain.filter

import com.futabooo.smstoslack.domain.model.FilterMode
import com.futabooo.smstoslack.domain.model.FilterRule
import com.futabooo.smstoslack.domain.model.FilterRuleType

object SmsFilterEvaluator {

    fun shouldForward(
        sender: String,
        body: String,
        filterMode: FilterMode,
        rules: List<FilterRule>
    ): Boolean {
        if (filterMode == FilterMode.DISABLED) return true

        val enabledRules = rules.filter { it.enabled }
        if (enabledRules.isEmpty()) return true

        val anyMatch = enabledRules.any { rule -> matches(rule, sender, body) }

        return when (filterMode) {
            FilterMode.WHITELIST -> anyMatch
            FilterMode.BLACKLIST -> !anyMatch
            FilterMode.DISABLED -> true
        }
    }

    private fun matches(rule: FilterRule, sender: String, body: String): Boolean {
        return when (rule.type) {
            FilterRuleType.SENDER -> sender.contains(rule.pattern)
            FilterRuleType.KEYWORD -> body.contains(rule.pattern, ignoreCase = true)
        }
    }
}
