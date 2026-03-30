package com.futabooo.smstoslack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.futabooo.smstoslack.data.local.entity.FilterRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FilterRuleDao {

    @Insert
    suspend fun insert(rule: FilterRuleEntity)

    @Query("DELETE FROM filter_rules WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE filter_rules SET enabled = :enabled WHERE id = :id")
    suspend fun updateEnabled(id: Long, enabled: Boolean)

    @Query("SELECT * FROM filter_rules ORDER BY id ASC")
    fun getAll(): Flow<List<FilterRuleEntity>>

    @Query("SELECT * FROM filter_rules WHERE enabled = 1")
    suspend fun getEnabledRules(): List<FilterRuleEntity>
}
