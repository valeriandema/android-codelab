package com.sap.codelab.view.create

import com.sap.codelab.domain.model.Memo
import com.sap.codelab.mvi.UiEffect
import com.sap.codelab.mvi.UiIntent
import com.sap.codelab.mvi.UiState

/**
 * MVI contract for the create-memo screen: the picked location and any validation errors.
 *
 * The title/description text lives in the input fields (ViewBinding) and is passed in with
 * [CreateMemoIntent.Save]; the state only tracks what the screen must re-render declaratively.
 */
internal data class CreateMemoState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val titleError: Boolean = false,
    val descriptionError: Boolean = false,
) : UiState {
    /** True once the user has picked a location on the map. */
    val hasLocation: Boolean get() = latitude != null && longitude != null
}

/** Everything the user can do while creating a memo. */
internal sealed interface CreateMemoIntent : UiIntent {
    /** The user tapped the "pick location" button. */
    data object PickLocation : CreateMemoIntent

    /** The user confirmed a location on the map. */
    data class LocationPicked(val latitude: Double, val longitude: Double) : CreateMemoIntent

    /** The user requested to save, supplying the current text of the input fields. */
    data class Save(val title: String, val description: String) : CreateMemoIntent
}

/** One-shot effects for the create-memo screen. */
internal sealed interface CreateMemoEffect : UiEffect {
    /** Open the map picker, centered on the already-picked location if there is one. */
    data class LaunchLocationPicker(val latitude: Double?, val longitude: Double?) : CreateMemoEffect

    /** Proactively request location/notification permissions so the geofence can be registered. */
    data object RequestPermissions : CreateMemoEffect

    /** The memo was persisted; the activity registers its geofence (if any) and finishes. */
    data class MemoSaved(val memo: Memo) : CreateMemoEffect
}
