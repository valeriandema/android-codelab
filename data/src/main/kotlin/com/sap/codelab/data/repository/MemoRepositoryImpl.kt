package com.sap.codelab.data.repository

import androidx.annotation.WorkerThread
import com.sap.codelab.data.database.MemoDao
import com.sap.codelab.data.mapper.toDomain
import com.sap.codelab.data.mapper.toEntity
import com.sap.codelab.domain.model.Memo
import com.sap.codelab.domain.repository.IMemoRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class MemoRepositoryImpl @Inject constructor(
    private val memoDao: MemoDao,
) : IMemoRepository {

    @WorkerThread
    override fun saveMemo(memo: Memo): Long = memoDao.insert(memo.toEntity())

    @WorkerThread
    override fun getAll(): List<Memo> = memoDao.getAll().map { it.toDomain() }

    @WorkerThread
    override fun getOpen(): List<Memo> = memoDao.getOpen().map { it.toDomain() }

    @WorkerThread
    override fun getMemoById(id: Long): Memo = memoDao.getMemoById(id).toDomain()
}
