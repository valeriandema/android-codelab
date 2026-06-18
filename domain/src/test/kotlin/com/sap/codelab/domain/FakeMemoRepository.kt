package com.sap.codelab.domain

import com.sap.codelab.domain.model.Memo
import com.sap.codelab.domain.repository.IMemoRepository

/**
 * In-memory [IMemoRepository] for unit tests. Records every saved memo and lets each query result be
 * stubbed independently.
 */
internal class FakeMemoRepository : IMemoRepository {

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
