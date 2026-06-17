package com.sap.codelab.notification

import com.sap.codelab.domain.notification.MemoReminder
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Binds the concrete [Notifier] to the [MemoReminder] port consumed by the geofence module.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class NotificationModule {

    @Binds
    abstract fun bindMemoReminder(notifier: Notifier): MemoReminder
}
