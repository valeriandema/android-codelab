package com.sap.codelab.view.map

import com.sap.codelab.mvi.UiEffect
import com.sap.codelab.mvi.UiIntent
import com.sap.codelab.mvi.UiState

/**
 * MVI contract for the Compose map picker.
 */
internal data class MapPickerState(
    /** The currently selected point, or null until the user taps the map. */
    val selected: LatLng? = null,
) : UiState

/** Intents for the map picker. */
internal sealed interface MapPickerIntent : UiIntent {
    /** Seed the picker with an optional pre-selected location (e.g. when editing). */
    data class Initialize(val initial: LatLng?) : MapPickerIntent

    /** The user tapped a point on the map. */
    data class MapClicked(val latLng: LatLng) : MapPickerIntent

    /** The user confirmed the current selection. */
    data object Confirm : MapPickerIntent
}

/** One-shot effects for the map picker. */
internal sealed interface MapPickerEffect : UiEffect {
    /** Return [picked] to the caller and close the picker. */
    data class Finish(val picked: LatLng) : MapPickerEffect
}
