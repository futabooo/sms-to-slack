package com.futabooo.smstoslack.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SmsMessageTest {

    @Test
    fun `SmsMessage holds sender body and timestamp`() {
        val message = SmsMessage(
            sender = "+819012345678",
            body = "Hello World",
            timestamp = 1708900000000L
        )
        assertEquals("+819012345678", message.sender)
        assertEquals("Hello World", message.body)
        assertEquals(1708900000000L, message.timestamp)
    }

    @Test
    fun `SmsMessage equality works correctly`() {
        val message1 = SmsMessage("+819012345678", "Hello", 1000L)
        val message2 = SmsMessage("+819012345678", "Hello", 1000L)
        val message3 = SmsMessage("+819087654321", "Hello", 1000L)

        assertEquals(message1, message2)
        assertNotEquals(message1, message3)
    }

    @Test
    fun `SmsMessage copy works correctly`() {
        val original = SmsMessage("+819012345678", "Original", 1000L)
        val copied = original.copy(body = "Copied")

        assertEquals("+819012345678", copied.sender)
        assertEquals("Copied", copied.body)
        assertEquals(1000L, copied.timestamp)
    }
}
