package com.sap.codelab.utils.extensions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [truncate], which produces the notification body (first 140 chars of a memo).
 */
class StringExtensionsTest {

    @Test
    fun `shorter than max is returned unchanged`() {
        val text = "A short memo"
        assertEquals(text, text.truncate(140))
    }

    @Test
    fun `exactly max is returned unchanged`() {
        val text = "x".repeat(140)
        assertEquals(text, text.truncate(140))
    }

    @Test
    fun `longer than max is cut and ellipsized`() {
        val text = "y".repeat(200)
        val result = text.truncate(140)
        assertTrue(result.endsWith("…"))
        // 140 kept characters + the ellipsis.
        assertEquals(141, result.length)
    }

    @Test
    fun `default limit is 140`() {
        val text = "z".repeat(141)
        assertEquals(141, text.truncate().length)
    }
}
