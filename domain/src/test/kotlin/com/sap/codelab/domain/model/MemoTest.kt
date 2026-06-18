package com.sap.codelab.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MemoTest {

    private fun memo(lat: Double, lng: Double) =
        Memo(id = 1, title = "t", description = "d", reminderDate = 0, reminderLatitude = lat, reminderLongitude = lng)

    @Test
    fun `zero zero means no location`() {
        assertFalse(memo(0.0, 0.0).hasLocation())
    }

    @Test
    fun `non-zero latitude counts as a location`() {
        assertTrue(memo(48.1, 0.0).hasLocation())
    }

    @Test
    fun `non-zero longitude counts as a location`() {
        assertTrue(memo(0.0, 11.6).hasLocation())
    }

    @Test
    fun `both coordinates set counts as a location`() {
        assertTrue(memo(48.1, 11.6).hasLocation())
    }
}
