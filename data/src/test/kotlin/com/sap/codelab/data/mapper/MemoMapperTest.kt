package com.sap.codelab.data.mapper

import com.sap.codelab.data.database.MemoEntity
import com.sap.codelab.domain.model.Memo
import org.junit.Assert.assertEquals
import org.junit.Test

class MemoMapperTest {

    private val domain = Memo(
        id = 5,
        title = "Title",
        description = "Description",
        reminderDate = 123L,
        reminderLatitude = 48.1,
        reminderLongitude = 11.6,
        isDone = true,
    )

    private val entity = MemoEntity(
        id = 5,
        title = "Title",
        description = "Description",
        reminderDate = 123L,
        reminderLatitude = 48.1,
        reminderLongitude = 11.6,
        isDone = true,
    )

    @Test
    fun `toEntity copies every field`() {
        assertEquals(entity, domain.toEntity())
    }

    @Test
    fun `toDomain copies every field`() {
        assertEquals(domain, entity.toDomain())
    }

    @Test
    fun `entity round-trips unchanged`() {
        assertEquals(entity, entity.toDomain().toEntity())
    }

    @Test
    fun `domain round-trips unchanged`() {
        assertEquals(domain, domain.toEntity().toDomain())
    }
}
