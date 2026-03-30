package com.futabooo.smstoslack.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.futabooo.smstoslack.domain.model.ForwardingStatus

@Entity(tableName = "forwarded_messages")
data class ForwardedMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String,
    val body: String,
    val timestamp: Long,
    val status: ForwardingStatus,
    @ColumnInfo(name = "response_code")
    val responseCode: Int?
)
