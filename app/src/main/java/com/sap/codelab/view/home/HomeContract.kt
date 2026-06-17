package com.sap.codelab.view.home

import com.sap.codelab.domain.model.Memo
import com.sap.codelab.mvi.UiEffect
import com.sap.codelab.mvi.UiIntent
import com.sap.codelab.mvi.UiState

/**
 * MVI contract for the Home screen: the list of memos and the open/all filter toggle.
 */
internal data class HomeState(
    val memos: List<Memo> = emptyList(),
    /** When true the list shows all memos; when false only open (not done) ones. Drives the menu. */
    val showingAll: Boolean = false,
) : UiState

/** Everything the user can do on the Home screen. */
internal sealed interface HomeIntent : UiIntent {
    /** Show only open memos (initial filter). */
    data object ShowOpen : HomeIntent

    /** Show all memos. */
    data object ShowAll : HomeIntent

    /** Re-load the list under the current filter (e.g. after returning from create). */
    data object Refresh : HomeIntent

    /** The user toggled a memo's "done" checkbox. */
    data class ToggleDone(val memo: Memo, val isDone: Boolean) : HomeIntent

    /** The user tapped a memo row to open its detail view. */
    data class OpenMemo(val memoId: Long) : HomeIntent

    /** The user tapped the FAB to create a new memo. */
    data object CreateMemo : HomeIntent
}

/** One-shot navigation effects for the Home screen. */
internal sealed interface HomeEffect : UiEffect {
    /** Navigate to the detail view of the memo with [memoId]. */
    data class NavigateToMemo(val memoId: Long) : HomeEffect

    /** Navigate to the create-memo screen. */
    data object NavigateToCreateMemo : HomeEffect
}
