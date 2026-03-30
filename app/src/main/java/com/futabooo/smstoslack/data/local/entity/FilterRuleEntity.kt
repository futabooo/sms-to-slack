package com.futabooo.smstoslack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.futabooo.smstoslack.domain.model.FilterRuleType

@Entity(tableName = "filter_rules")
data class FilterRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: FilterRuleType,
    val pattern: String,
    val enabled: Boolean = true
)
