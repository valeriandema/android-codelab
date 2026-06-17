package com.sap.codelab.view.map

import com.sap.codelab.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * MVI ViewModel for the Compose map picker ([MapPickerScreen]). Tracks the selected point and emits
 * a [MapPickerEffect.Finish] when the user confirms. Instantiated by Hilt.
 */
@HiltViewModel
internal class MapPickerViewModel @Inject constructor() :
    MviViewModel<MapPickerIntent, MapPickerState, MapPickerEffect>(MapPickerState()) {

    override fun onIntent(intent: MapPickerIntent) {
        when (intent) {
            is MapPickerIntent.Initialize -> setState { copy(selected = intent.initial) }
            is MapPickerIntent.MapClicked -> setState { copy(selected = intent.latLng) }
            MapPickerIntent.Confirm -> currentState.selected?.let { sendEffect(MapPickerEffect.Finish(it)) }
        }
    }
}
