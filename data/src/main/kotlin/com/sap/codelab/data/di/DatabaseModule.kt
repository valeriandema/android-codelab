package com.sap.codelab.data.di

import android.content.Context
import androidx.room.Room
import com.sap.codelab.data.database.MemoDao
import com.sap.codelab.data.database.MemoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATABASE_NAME: String = "codelab"

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MemoDatabase =
        Room.databaseBuilder(context, MemoDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideMemoDao(database: MemoDatabase): MemoDao = database.getMemoDao()
}
