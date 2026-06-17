package com.sap.codelab.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * The Dao representation of a Memo.
 */
@Dao
internal interface MemoDao {

    /**
     * @return all memos that are currently in the database.
     */
    @Query("SELECT * FROM memo")
    fun getAll(): List<MemoEntity>

    /**
     * @return all memos that are currently in the database and have not yet been marked as "done".
     */
    @Query("SELECT * FROM memo WHERE isDone = 0")
    fun getOpen(): List<MemoEntity>

    /**
     * Inserts the given Memo into the database.
     *
     * @return the row id (= memo id) of the inserted memo.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(memo: MemoEntity): Long

    /**
     * @return the memo whose id matches the given id.
     */
    @Query("SELECT * FROM memo WHERE id = :memoId")
    fun getMemoById(memoId: Long): MemoEntity
}
