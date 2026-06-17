package com.sap.codelab.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Geofences registered with Google Play Services are cleared on reboot. This receiver re-registers
 * the geofences for all open memos once the device finishes booting, so reminders keep working.
 * The [GeofenceManager] is injected by Hilt.
 */
@AndroidEntryPoint
internal class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var geofenceManager: GeofenceManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            geofenceManager.registerAll()
        }
    }
}
