package com.sap.codelab.domain.repository

import com.sap.codelab.domain.model.Memo

/**
 * Abstraction over memo storage. Declared by the domain layer and implemented by `:data`, so the
 * dependency points inward: callers depend on this interface, never on a concrete data source.
 */
interface IMemoRepository {

    /**
     * Saves the given memo.
     *
     * @return the id assigned to the saved memo.
     */
    fun saveMemo(memo: Memo): Long

    /**
     * @return all memos.
     */
    fun getAll(): List<Memo>

    /**
     * @return all memos except those marked as "done".
     */
    fun getOpen(): List<Memo>

    /**
     * @return the memo whose id matches the given id.
     */
    fun getMemoById(id: Long): Memo
}
