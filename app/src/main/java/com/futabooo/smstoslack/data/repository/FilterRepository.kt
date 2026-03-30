package com.futabooo.smstoslack.data.repository

import com.futabooo.smstoslack.data.local.dao.FilterRuleDao
import com.futabooo.smstoslack.data.local.entity.FilterRuleEntity
import com.futabooo.smstoslack.domain.model.FilterRule
import com.futabooo.smstoslack.domain.model.FilterRuleType
import kotlinx.coroutines.flow.Flow

class FilterRepository(private val dao: FilterRuleDao) {

    val allRulesFlow: Flow<List<FilterRuleEntity>> = dao.getAll()

    suspend fun addRule(type: FilterRuleType, pattern: String) {
        dao.insert(FilterRuleEntity(type = type, pattern = pattern))
    }

    suspend fun deleteRule(id: Long) {
        dao.deleteById(id)
    }

    suspend fun updateRuleEnabled(id: Long, enabled: Boolean) {
        dao.updateEnabled(id, enabled)
    }

    suspend fun getEnabledRules(): List<FilterRule> {
        return dao.getEnabledRules().map { entity ->
            FilterRule(
                id = entity.id,
                type = entity.type,
                pattern = entity.pattern,
                enabled = entity.enabled
            )
        }
    }
}
