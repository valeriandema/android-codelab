package com.sap.codelab.view.create

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateMemoStateTest {

    @Test
    fun `hasLocation is false until both coordinates are set`() {
        assertFalse(CreateMemoState().hasLocation)
        assertFalse(CreateMemoState(latitude = 48.1).hasLocation)
        assertFalse(CreateMemoState(longitude = 11.6).hasLocation)
    }

    @Test
    fun `hasLocation is true once both coordinates are set`() {
        assertTrue(CreateMemoState(latitude = 48.1, longitude = 11.6).hasLocation)
    }
}
