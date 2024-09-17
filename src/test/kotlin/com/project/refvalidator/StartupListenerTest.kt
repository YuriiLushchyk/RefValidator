package com.project.refvalidator

import com.project.refvalidator.StartupListener.Companion.splitRange
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StartupListenerTest {

    @Test
    fun `test splitRange`() {
        val pair = 3L to 13L
        val split = pair.splitRange(3)
        val expected = listOf(3L to 5L, 6L to 8L, 9L to 11L, 12L to 13L)
        assertEquals(expected, split)
    }
}