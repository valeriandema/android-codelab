package com.sap.codelab.mvi

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Marker for an immutable UI state that fully describes what a screen renders. There is exactly one
 * state object per screen at any moment; the view is a pure function of it.
 */
internal interface UiState

/**
 * Marker for a user intent: something the user (or system) wants to happen. Intents are the *only*
 * way a view talks to its [MviViewModel] — there are no other public methods.
 */
internal interface UiIntent

/**
 * Marker for a one-shot side effect (navigation, finishing the activity, a transient message).
 * Effects are consumed exactly once and, unlike [UiState], are not replayed on configuration change.
 */
internal interface UiEffect

/**
 * Base class for the Model-View-Intent pattern used across the app's screens.
 *
 * A subclass owns a single immutable [state] and reacts to [UiIntent]s through the one entry point
 * [onIntent]. It mutates state via [setState] and emits navigation/side effects via [sendEffect].
 * The view observes [state] (and [effects]) and renders; it never reaches into the ViewModel for
 * anything else. This keeps the data flow unidirectional: view → intent → reducer → state → view.
 *
 * @param I the screen's intent type.
 * @param S the screen's state type.
 * @param E the screen's effect type.
 */
internal abstract class MviViewModel<I : UiIntent, S : UiState, E : UiEffect>(
    initialState: S,
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)

    /** The current screen state. The view collects this and renders it. */
    val state: StateFlow<S> = _state.asStateFlow()

    // BUFFERED so effects emitted before the view starts collecting are not dropped.
    private val _effects = Channel<E>(Channel.BUFFERED)

    /** A stream of one-shot effects. Each effect is delivered to a single collector exactly once. */
    val effects: Flow<E> = _effects.receiveAsFlow()

    /** The latest state, for reducers/handlers that need to read before they write. */
    protected val currentState: S get() = _state.value

    /**
     * The single entry point for everything a view wants to do. Implementations typically `when` over
     * the sealed intent type and call [setState] / [sendEffect] accordingly.
     */
    abstract fun onIntent(intent: I)

    /** Atomically replaces the current state with the result of [reducer]. */
    protected fun setState(reducer: S.() -> S) {
        _state.update(reducer)
    }

    /** Enqueues a one-shot [effect] for the view to consume. */
    protected fun sendEffect(effect: E) {
        viewModelScope.launch { _effects.send(effect) }
    }
}

/**
 * Collects [flow] only while this activity is at least STARTED, automatically pausing on stop and
 * resuming on restart. This is the safe way to observe MVI [MviViewModel.state] and
 * [MviViewModel.effects] from an activity.
 */
internal fun <T> AppCompatActivity.collectWhileStarted(
    flow: Flow<T>,
    action: suspend (T) -> Unit,
) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect(action)
        }
    }
}
