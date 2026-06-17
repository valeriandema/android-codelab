package com.sap.codelab.domain.model

/**
 * Represents a memo. This is the domain model: a plain immutable Kotlin type with no Android or
 * persistence concerns. The data layer maps it to/from its own Room entity.
 */
data class Memo(
    val id: Long,
    val title: String,
    val description: String,
    val reminderDate: Long,
    val reminderLatitude: Double,
    val reminderLongitude: Double,
    val isDone: Boolean = false,
) {

    /**
     * @return true if this memo has a location attached (i.e. coordinates were picked on the map).
     * The coordinates (0.0, 0.0) are treated as "no location".
     */
    fun hasLocation(): Boolean = reminderLatitude != 0.0 || reminderLongitude != 0.0
}
