package com.sap.codelab.view.create

import androidx.lifecycle.viewModelScope
import com.sap.codelab.di.IoDispatcher
import com.sap.codelab.domain.usecase.SaveMemoResult
import com.sap.codelab.domain.usecase.SaveMemoUseCase
import com.sap.codelab.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * MVI ViewModel for the create-memo screen. Holds the picked location and delegates validation +
 * persistence to [SaveMemoUseCase], exposing progress through [CreateMemoState] and [CreateMemoEffect].
 */
@HiltViewModel
internal class CreateMemoViewModel @Inject constructor(
    private val saveMemo: SaveMemoUseCase,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : MviViewModel<CreateMemoIntent, CreateMemoState, CreateMemoEffect>(CreateMemoState()) {

    override fun onIntent(intent: CreateMemoIntent) {
        when (intent) {
            CreateMemoIntent.PickLocation ->
                sendEffect(CreateMemoEffect.LaunchLocationPicker(currentState.latitude, currentState.longitude))

            is CreateMemoIntent.LocationPicked -> {
                setState { copy(latitude = intent.latitude, longitude = intent.longitude) }
                // Get permissions out of the way while the activity is still open.
                sendEffect(CreateMemoEffect.RequestPermissions)
            }

            is CreateMemoIntent.Save -> save(intent)
        }
    }

    private fun save(intent: CreateMemoIntent.Save) {
        viewModelScope.launch {
            val result = withContext(ioDispatcher) {
                saveMemo(intent.title, intent.description, currentState.latitude, currentState.longitude)
            }
            when (result) {
                is SaveMemoResult.Invalid ->
                    setState { copy(titleError = result.titleBlank, descriptionError = result.descriptionBlank) }

                is SaveMemoResult.Success -> {
                    setState { copy(titleError = false, descriptionError = false) }
                    sendEffect(CreateMemoEffect.MemoSaved(result.memo))
                }
            }
        }
    }
}
