package com.sap.codelab.view.home

import androidx.lifecycle.viewModelScope
import com.sap.codelab.di.IoDispatcher
import com.sap.codelab.domain.repository.IMemoRepository
import com.sap.codelab.domain.usecase.MarkMemoDoneUseCase
import com.sap.codelab.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MVI ViewModel for the Home screen. Reacts to [HomeIntent]s, loads memos into [HomeState] and emits
 * [HomeEffect]s for navigation.
 */
@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val repository: IMemoRepository,
    private val markMemoDone: MarkMemoDoneUseCase,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : MviViewModel<HomeIntent, HomeState, HomeEffect>(HomeState()) {

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.ShowOpen -> loadMemos(showAll = false)
            HomeIntent.ShowAll -> loadMemos(showAll = true)
            HomeIntent.Refresh -> loadMemos(showAll = currentState.showingAll)
            is HomeIntent.ToggleDone -> toggleDone(intent)
            is HomeIntent.OpenMemo -> sendEffect(HomeEffect.NavigateToMemo(intent.memoId))
            HomeIntent.CreateMemo -> sendEffect(HomeEffect.NavigateToCreateMemo)
        }
    }

    private fun loadMemos(showAll: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            val memos = if (showAll) repository.getAll() else repository.getOpen()
            setState { copy(memos = memos, showingAll = showAll) }
        }
    }

    // The checkbox only ever moves to "done"; unchecking is a no-op for persistence.
    private fun toggleDone(intent: HomeIntent.ToggleDone) {
        if (!intent.isDone) return
        viewModelScope.launch(ioDispatcher) {
            markMemoDone(intent.memo)
            val showAll = currentState.showingAll
            val memos = if (showAll) repository.getAll() else repository.getOpen()
            setState { copy(memos = memos, showingAll = showAll) }
        }
    }
}
