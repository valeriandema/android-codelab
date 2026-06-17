package com.sap.codelab.domain.usecase

import com.sap.codelab.domain.model.Memo
import com.sap.codelab.domain.repository.IMemoRepository
import javax.inject.Inject

/**
 * Validates the supplied input and, if valid, builds and persists a memo. Validation lives here
 * (not in the ViewModel) so the rule "a memo needs a non-blank title and description" is part of the
 * domain.
 */
class SaveMemoUseCase @Inject constructor(
    private val repository: IMemoRepository,
) {

    /**
     * @return [SaveMemoResult.Success] with the saved memo (including its new id) when the input is
     * valid, or [SaveMemoResult.Invalid] describing which fields were blank.
     */
    operator fun invoke(
        title: String,
        description: String,
        latitude: Double?,
        longitude: Double?,
    ): SaveMemoResult {
        val titleBlank = title.isBlank()
        val descriptionBlank = description.isBlank()
        if (titleBlank || descriptionBlank) {
            return SaveMemoResult.Invalid(titleBlank, descriptionBlank)
        }
        val memo = Memo(
            id = 0,
            title = title,
            description = description,
            reminderDate = 0,
            reminderLatitude = latitude ?: 0.0,
            reminderLongitude = longitude ?: 0.0,
            isDone = false,
        )
        val id = repository.saveMemo(memo)
        return SaveMemoResult.Success(memo.copy(id = id))
    }
}

/** Outcome of [SaveMemoUseCase]. */
sealed interface SaveMemoResult {

    /** The memo was persisted; [memo] carries the assigned id. */
    data class Success(val memo: Memo) : SaveMemoResult

    /** Validation failed; the flags say which fields were blank. */
    data class Invalid(val titleBlank: Boolean, val descriptionBlank: Boolean) : SaveMemoResult
}
