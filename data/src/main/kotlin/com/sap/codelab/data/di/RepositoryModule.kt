package com.sap.codelab.data.di

import com.sap.codelab.data.repository.MemoRepositoryImpl
import com.sap.codelab.domain.repository.IMemoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMemoRepository(impl: MemoRepositoryImpl): IMemoRepository
}
