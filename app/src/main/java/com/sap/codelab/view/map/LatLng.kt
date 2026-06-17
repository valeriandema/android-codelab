package com.sap.codelab.view.map

/**
 * A simple latitude/longitude pair used to pass a picked location between the map picker and its
 * caller. Kept independent of any map library so the rest of the app does not depend on osmdroid (or
 * any other) map types.
 */
internal data class LatLng(
    val latitude: Double,
    val longitude: Double,
)
