package com.sap.codelab.domain.usecase

import com.sap.codelab.domain.model.Memo
import com.sap.codelab.domain.repository.IMemoRepository
import javax.inject.Inject

/**
 * Marks a memo as done. The app only supports completing a memo, never re-opening it, so this only
 * ever sets [Memo.isDone] to true.
 */
class MarkMemoDoneUseCase @Inject constructor(
    private val repository: IMemoRepository,
) {
    operator fun invoke(memo: Memo) {
        repository.saveMemo(memo.copy(isDone = true))
    }
}
