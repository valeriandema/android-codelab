package com.sap.codelab.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Provides the application-wide [CoroutineScope]. This is reserved for application-scoped singletons
 * (e.g. [com.sap.codelab.geofence.GeofenceManager]) that run work with no UI lifecycle of their own;
 * A [SupervisorJob] keeps the scope alive when one of its children fails.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object CoroutinesModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob())

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
