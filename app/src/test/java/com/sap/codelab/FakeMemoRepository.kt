package com.sap.codelab

import com.sap.codelab.domain.model.Memo
import com.sap.codelab.domain.repository.IMemoRepository

/**
 * In-memory [IMemoRepository] for ViewModel unit tests.
 */
class FakeMemoRepository : IMemoRepository {

    val saved = mutableListOf<Memo>()
    var nextId: Long = 1
    var allMemos: List<Memo> = emptyList()
    var openMemos: List<Memo> = emptyList()
    var memoById: Memo? = null

    override fun saveMemo(memo: Memo): Long {
        saved.add(memo)
        return nextId
    }

    override fun getAll(): List<Memo> = allMemos

    override fun getOpen(): List<Memo> = openMemos

    override fun getMemoById(id: Long): Memo = memoById ?: error("memoById not stubbed")
}

internal fun memo(
    id: Long = 1,
    title: String = "t$id",
    description: String = "d$id",
    isDone: Boolean = false,
    latitude: Double = 0.0,
    longitude: Double = 0.0,
): Memo = Memo(
    id = id,
    title = title,
    description = description,
    reminderDate = 0,
    reminderLatitude = latitude,
    reminderLongitude = longitude,
    isDone = isDone,
)
