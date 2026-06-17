package com.sap.codelab.geofence

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.sap.codelab.di.ApplicationScope
import com.sap.codelab.di.IoDispatcher
import com.sap.codelab.domain.model.Memo
import com.sap.codelab.domain.repository.IMemoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GeofenceManager"

/**
 * Registers and removes geofences for memos. A single [PendingIntent] targeting
 * [GeofenceBroadcastReceiver] is shared by all geofences; the receiver identifies the triggered
 * memo via the geofence request id (which equals the memo id).
 *
 * Geofences are held by Google Play Services, so transitions are delivered even when the app is in
 * the background or not running. They do not survive a reboot, so [registerAll] is also called from
 * [BootReceiver].
 *
 * The application [Context], the [IMemoRepository] and the application [CoroutineScope] are all
 * injected by Hilt.
 */
@Singleton
class GeofenceManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repository: IMemoRepository,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    private val client: GeofencingClient
        get() = LocationServices.getGeofencingClient(context)

    private fun geofencePendingIntent(): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Geofencing requires a mutable PendingIntent so Play Services can attach the event.
            flags = flags or PendingIntent.FLAG_MUTABLE
        }
        return PendingIntent.getBroadcast(context, 0, intent, flags)
    }

    private fun buildGeofence(memo: Memo): Geofence =
        Geofence.Builder()
            .setRequestId(memo.id.toString())
            .setCircularRegion(memo.reminderLatitude, memo.reminderLongitude, GEOFENCE_RADIUS_IN_METERS)
            .setExpirationDuration(GEOFENCE_EXPIRATION)
            .setTransitionTypes(GEOFENCE_TRANSITION)
            .build()

    /**
     * Registers a geofence for the given memo if it has a location. Caller must ensure the location
     * permission is granted; if it is not, the call is skipped and a warning is logged.
     */
    fun addGeofence(memo: Memo) {
        if (!memo.hasLocation()) return
        if (!hasLocationPermission()) {
            Log.w(TAG, "Location permission missing; skipping geofence for memo ${memo.id}")
            return
        }
        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(buildGeofence(memo))
            .build()
        try {
            client.addGeofences(request, geofencePendingIntent())
                .addOnSuccessListener { Log.d(TAG, "Geofence added for memo ${memo.id}") }
                .addOnFailureListener { Log.e(TAG, "Failed to add geofence for memo ${memo.id}", it) }
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission revoked; cannot add geofence for memo ${memo.id}", e)
            ContextCompat.getMainExecutor(context).execute {
                Toast.makeText(
                    context,
                    "Enable location permissions to add location reminders",
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

    /**
     * Removes the geofence associated with the given memo id.
     */
    fun removeGeofence(memoId: Long) {
        client.removeGeofences(listOf(memoId.toString()))
            .addOnFailureListener { Log.e(TAG, "Failed to remove geofence for memo $memoId", it) }
    }

    /**
     * Re-registers geofences for all open memos that have a location. Used after the location
     * permission is granted and after a device reboot. The memos are read off the main thread.
     */
    fun registerAll() {
        applicationScope.launch(ioDispatcher) {
            repository.getOpen()
                .filter { it.hasLocation() }
                .forEach { addGeofence(it) }
        }
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
}
