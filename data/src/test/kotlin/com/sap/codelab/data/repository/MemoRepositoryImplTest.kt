package com.sap.codelab.data.repository

import com.sap.codelab.data.database.FakeMemoDao
import com.sap.codelab.data.database.MemoEntity
import com.sap.codelab.domain.model.Memo
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MemoRepositoryImplTest {

    private lateinit var dao: FakeMemoDao
    private lateinit var repository: MemoRepositoryImpl

    @Before
    fun setUp() {
        dao = FakeMemoDao()
        repository = MemoRepositoryImpl(dao)
    }

    private fun entity(id: Long, isDone: Boolean = false) =
        MemoEntity(id, "t$id", "d$id", 0, 0.0, 0.0, isDone)

    @Test
    fun `saveMemo maps to an entity, inserts it and returns the new id`() {
        dao.insertReturnId = 99
        val memo = Memo(0, "Title", "Desc", 0, 0.0, 0.0)

        val id = repository.saveMemo(memo)

        assertEquals(99L, id)
        val insertedEntity = dao.inserted.single()
        assertEquals("Title", insertedEntity.title)
        assertEquals("Desc", insertedEntity.description)
    }

    @Test
    fun `getAll maps every entity to the domain model`() {
        dao.allMemos = listOf(entity(1), entity(2))

        val result = repository.getAll()

        assertEquals(listOf(1L, 2L), result.map { it.id })
        assertEquals("t1", result.first().title)
    }

    @Test
    fun `getOpen maps every entity to the domain model`() {
        dao.openMemos = listOf(entity(3))

        val result = repository.getOpen()

        assertEquals(1, result.size)
        assertEquals(3L, result.single().id)
    }

    @Test
    fun `getMemoById maps the entity to the domain model`() {
        dao.byId = entity(7, isDone = true)

        val result = repository.getMemoById(7)

        assertEquals(7L, result.id)
        assertEquals(true, result.isDone)
    }
}
