package com.sap.codelab.domain.usecase

import com.sap.codelab.domain.FakeMemoRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SaveMemoUseCaseTest {

    private lateinit var repository: FakeMemoRepository
    private lateinit var saveMemo: SaveMemoUseCase

    @Before
    fun setUp() {
        repository = FakeMemoRepository()
        saveMemo = SaveMemoUseCase(repository)
    }

    @Test
    fun `blank title is rejected without persisting`() {
        val result = saveMemo(title = "   ", description = "desc", latitude = null, longitude = null)

        result as SaveMemoResult.Invalid
        assertTrue(result.titleBlank)
        assertFalse(result.descriptionBlank)
        assertTrue(repository.saved.isEmpty())
    }

    @Test
    fun `blank description is rejected without persisting`() {
        val result = saveMemo(title = "title", description = "", latitude = null, longitude = null)

        result as SaveMemoResult.Invalid
        assertFalse(result.titleBlank)
        assertTrue(result.descriptionBlank)
        assertTrue(repository.saved.isEmpty())
    }

    @Test
    fun `both blank are both flagged`() {
        val result = saveMemo(title = "", description = "  ", latitude = null, longitude = null)

        result as SaveMemoResult.Invalid
        assertTrue(result.titleBlank)
        assertTrue(result.descriptionBlank)
    }

    @Test
    fun `valid input persists the memo and returns it with the assigned id`() {
        repository.nextId = 42

        val result = saveMemo(title = "Buy milk", description = "2%", latitude = null, longitude = null)

        result as SaveMemoResult.Success
        assertEquals(42L, result.memo.id)
        assertEquals("Buy milk", result.memo.title)
        assertEquals("2%", result.memo.description)
        assertFalse(result.memo.isDone)
        assertEquals(1, repository.saved.size)
        // The memo handed to the repository has the pre-insert id of 0.
        assertEquals(0L, repository.saved.single().id)
    }

    @Test
    fun `null coordinates are stored as zero meaning no location`() {
        val result = saveMemo(title = "t", description = "d", latitude = null, longitude = null)

        result as SaveMemoResult.Success
        assertEquals(0.0, result.memo.reminderLatitude, 0.0)
        assertEquals(0.0, result.memo.reminderLongitude, 0.0)
        assertFalse(result.memo.hasLocation())
    }

    @Test
    fun `supplied coordinates are persisted on the memo`() {
        val result = saveMemo(title = "t", description = "d", latitude = 48.1, longitude = 11.6)

        result as SaveMemoResult.Success
        assertEquals(48.1, result.memo.reminderLatitude, 0.0)
        assertEquals(11.6, result.memo.reminderLongitude, 0.0)
        assertTrue(result.memo.hasLocation())
    }
}
