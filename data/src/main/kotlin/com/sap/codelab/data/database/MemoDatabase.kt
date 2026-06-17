package com.sap.codelab.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MemoEntity::class], version = 1, exportSchema = false)
internal abstract class MemoDatabase : RoomDatabase() {

    abstract fun getMemoDao(): MemoDao
}
