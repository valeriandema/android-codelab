package com.sap.codelab.view.detail

import com.sap.codelab.domain.model.Memo
import com.sap.codelab.mvi.UiEffect
import com.sap.codelab.mvi.UiIntent
import com.sap.codelab.mvi.UiState

/**
 * MVI contract for the read-only memo detail screen.
 */
internal data class ViewMemoState(
    /** The memo being displayed, or null until it has been loaded. */
    val memo: Memo? = null,
) : UiState

/** Intents for the detail screen. */
internal sealed interface ViewMemoIntent : UiIntent {
    /** Load the memo with the given id for display. */
    data class Load(val memoId: Long) : ViewMemoIntent
}

/** The detail screen is read-only and emits no one-shot effects. */
internal sealed interface ViewMemoEffect : UiEffect
