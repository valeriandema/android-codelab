package com.sap.codelab.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.sap.codelab.di.IoDispatcher
import com.sap.codelab.domain.notification.MemoReminder
import com.sap.codelab.domain.repository.IMemoRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "GeofenceReceiver"

/**
 * Receives geofence transition events from Google Play Services and posts a reminder notification
 * for each memo whose location was reached. Delivered even when the app is in the background or not
 * running. Dependencies are injected by Hilt.
 */
@AndroidEntryPoint
internal class GeofenceBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: IMemoRepository

    @Inject
    lateinit var reminder: MemoReminder

    @Inject
    lateinit var geofenceManager: GeofenceManager

    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent)
        if (event == null) {
            Log.e(TAG, "Received null GeofencingEvent")
            return
        }
        if (event.hasError()) {
            Log.e(TAG, "Geofencing error: ${GeofenceStatusCodes.getStatusCodeString(event.errorCode)}")
            return
        }
        if (event.geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER) return

        val memoIds = event.triggeringGeofences
            ?.mapNotNull { it.requestId.toLongOrNull() }
            ?: return
        if (memoIds.isEmpty()) return

        // DB access must run off the main thread; goAsync keeps the receiver alive while we do it.
        val pendingResult = goAsync()
        CoroutineScope(ioDispatcher).launch {
            try {
                memoIds.forEach { id ->
                    val memo = repository.getMemoById(id)
                    // Skip memos the user already completed; also drop their stale geofence.
                    if (memo.isDone) {
                        geofenceManager.removeGeofence(id)
                    } else {
                        reminder.showMemoReminder(memo)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to show reminder", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
