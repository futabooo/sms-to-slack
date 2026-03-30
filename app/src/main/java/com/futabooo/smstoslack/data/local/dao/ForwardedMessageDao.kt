package com.futabooo.smstoslack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.futabooo.smstoslack.data.local.entity.ForwardedMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ForwardedMessageDao {

    @Insert
    suspend fun insert(message: ForwardedMessageEntity)

    @Query("SELECT * FROM forwarded_messages ORDER BY timestamp DESC")
    fun getRecentMessages(): Flow<List<ForwardedMessageEntity>>

    @Query("DELETE FROM forwarded_messages WHERE id NOT IN (SELECT id FROM forwarded_messages ORDER BY timestamp DESC LIMIT 1000)")
    suspend fun trimOldMessages()
}
