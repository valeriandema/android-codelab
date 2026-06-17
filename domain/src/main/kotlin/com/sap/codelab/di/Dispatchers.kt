package com.sap.codelab.di

import javax.inject.Qualifier

/**
 * Qualifies the [kotlinx.coroutines.Dispatchers.IO] dispatcher provided by the app's coroutines
 * module. Inject this instead of referencing the dispatcher directly so it can be swapped in tests.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
