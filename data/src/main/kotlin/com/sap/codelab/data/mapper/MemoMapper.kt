package com.sap.codelab.data.mapper

import com.sap.codelab.data.database.MemoEntity
import com.sap.codelab.domain.model.Memo

internal fun MemoEntity.toDomain(): Memo = Memo(
    id = id,
    title = title,
    description = description,
    reminderDate = reminderDate,
    reminderLatitude = reminderLatitude,
    reminderLongitude = reminderLongitude,
    isDone = isDone,
)

internal fun Memo.toEntity(): MemoEntity = MemoEntity(
    id = id,
    title = title,
    description = description,
    reminderDate = reminderDate,
    reminderLatitude = reminderLatitude,
    reminderLongitude = reminderLongitude,
    isDone = isDone,
)
