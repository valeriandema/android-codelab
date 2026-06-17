package com.sap.codelab.di

import javax.inject.Qualifier

/**
 * Qualifies the application-wide [kotlinx.coroutines.CoroutineScope] provided by the app's
 * coroutines module. This scope lives for the whole application lifetime and is used for top-level
 * coroutines that must not be cancelled when a screen is torn down (e.g. persisting a memo).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
