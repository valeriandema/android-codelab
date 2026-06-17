package com.sap.codelab.utils.permissions

import android.Manifest
import android.os.Build

/**
 * The permissions needed for location-based memo reminders: fine + coarse location, plus (on
 * Android 13+) the permission to post the reminder notification.
 */
internal fun locationReminderPermissions(): Array<String> = buildList {
    add(Manifest.permission.ACCESS_FINE_LOCATION)
    add(Manifest.permission.ACCESS_COARSE_LOCATION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.POST_NOTIFICATIONS)
    }
}.toTypedArray()
