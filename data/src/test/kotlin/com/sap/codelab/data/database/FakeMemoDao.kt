package com.sap.codelab.data.database

/**
 * In-memory [MemoDao] for unit tests. Records inserts and serves stubbed query results.
 */
internal class FakeMemoDao : MemoDao {

    val inserted = mutableListOf<MemoEntity>()
    var insertReturnId: Long = 1
    var allMemos: List<MemoEntity> = emptyList()
    var openMemos: List<MemoEntity> = emptyList()
    var byId: MemoEntity? = null

    override fun getAll(): List<MemoEntity> = allMemos

    override fun getOpen(): List<MemoEntity> = openMemos

    override fun insert(memo: MemoEntity): Long {
        inserted.add(memo)
        return insertReturnId
    }

    override fun getMemoById(memoId: Long): MemoEntity = byId ?: error("byId not stubbed")
}
