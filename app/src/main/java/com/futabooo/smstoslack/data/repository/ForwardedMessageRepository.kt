package com.futabooo.smstoslack.data.repository

import com.futabooo.smstoslack.data.local.dao.ForwardedMessageDao
import com.futabooo.smstoslack.data.local.entity.ForwardedMessageEntity
import com.futabooo.smstoslack.domain.model.ForwardingStatus
import kotlinx.coroutines.flow.Flow

class ForwardedMessageRepository(private val dao: ForwardedMessageDao) {

    fun getRecentMessages(): Flow<List<ForwardedMessageEntity>> {
        return dao.getRecentMessages()
    }

    suspend fun save(
        sender: String,
        body: String,
        timestamp: Long,
        status: ForwardingStatus,
        responseCode: Int?
    ) {
        dao.insert(
            ForwardedMessageEntity(
                sender = sender,
                body = body,
                timestamp = timestamp,
                status = status,
                responseCode = responseCode
            )
        )
        dao.trimOldMessages()
    }
}
