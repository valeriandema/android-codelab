package com.sap.codelab.geofence

import com.google.android.gms.location.Geofence

/**
 * Radius around the memo location, in meters, within which the reminder fires.
 * The spec defines "reaching the location" as being within 200 meters of the selected point.
 */
internal const val GEOFENCE_RADIUS_IN_METERS: Float = 200f

/**
 * Transition that triggers the reminder: the user entering the geofence area.
 */
internal const val GEOFENCE_TRANSITION: Int = Geofence.GEOFENCE_TRANSITION_ENTER

/**
 * Geofences never expire on their own; they are removed explicitly (or on reboot / app data clear).
 */
internal const val GEOFENCE_EXPIRATION: Long = Geofence.NEVER_EXPIRE
