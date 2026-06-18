package com.sap.codelab.domain.usecase

import com.sap.codelab.domain.FakeMemoRepository
import com.sap.codelab.domain.model.Memo
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MarkMemoDoneUseCaseTest {

    private lateinit var repository: FakeMemoRepository
    private lateinit var markMemoDone: MarkMemoDoneUseCase

    @Before
    fun setUp() {
        repository = FakeMemoRepository()
        markMemoDone = MarkMemoDoneUseCase(repository)
    }

    @Test
    fun `marks the memo done and persists the change`() {
        val memo = Memo(
            id = 7,
            title = "t",
            description = "d",
            reminderDate = 0,
            reminderLatitude = 0.0,
            reminderLongitude = 0.0,
            isDone = false,
        )

        markMemoDone(memo)

        val saved = repository.saved.single()
        assertTrue(saved.isDone)
        // Identity and other fields are preserved.
        org.junit.Assert.assertEquals(7L, saved.id)
        org.junit.Assert.assertEquals("t", saved.title)
    }

    @Test
    fun `an already-done memo stays done`() {
        val memo = Memo(8, "t", "d", 0, 0.0, 0.0, isDone = true)

        markMemoDone(memo)

        assertTrue(repository.saved.single().isDone)
    }
}
