package com.futabooo.smstoslack.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SmsPermissionStatusTest {

    @Test
    fun `SmsPermissionStatus has three values`() {
        val values = SmsPermissionStatus.entries
        assertEquals(3, values.size)
    }

    @Test
    fun `SmsPermissionStatus contains GRANTED DENIED and PERMANENTLY_DENIED`() {
        assertEquals(SmsPermissionStatus.GRANTED, SmsPermissionStatus.valueOf("GRANTED"))
        assertEquals(SmsPermissionStatus.DENIED, SmsPermissionStatus.valueOf("DENIED"))
        assertEquals(SmsPermissionStatus.PERMANENTLY_DENIED, SmsPermissionStatus.valueOf("PERMANENTLY_DENIED"))
    }
}
