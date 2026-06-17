package com.sap.codelab.view.detail

import androidx.lifecycle.viewModelScope
import com.sap.codelab.di.IoDispatcher
import com.sap.codelab.domain.repository.IMemoRepository
import com.sap.codelab.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ViewMemoViewModel @Inject constructor(
    private val repository: IMemoRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : MviViewModel<ViewMemoIntent, ViewMemoState, ViewMemoEffect>(ViewMemoState()) {

    override fun onIntent(intent: ViewMemoIntent) {
        when (intent) {
            is ViewMemoIntent.Load -> loadMemo(intent.memoId)
        }
    }

    private fun loadMemo(memoId: Long) {
        viewModelScope.launch(ioDispatcher) {
            val memo = repository.getMemoById(memoId)
            setState { copy(memo = memo) }
        }
    }
}
