package com.futabooo.smstoslack.data.local

import androidx.room.TypeConverter
import com.futabooo.smstoslack.domain.model.FilterRuleType
import com.futabooo.smstoslack.domain.model.ForwardingStatus

class Converters {

    @TypeConverter
    fun fromForwardingStatus(status: ForwardingStatus): String {
        return status.name
    }

    @TypeConverter
    fun toForwardingStatus(value: String): ForwardingStatus {
        return ForwardingStatus.valueOf(value)
    }

    @TypeConverter
    fun fromFilterRuleType(type: FilterRuleType): String {
        return type.name
    }

    @TypeConverter
    fun toFilterRuleType(value: String): FilterRuleType {
        return FilterRuleType.valueOf(value)
    }
}
