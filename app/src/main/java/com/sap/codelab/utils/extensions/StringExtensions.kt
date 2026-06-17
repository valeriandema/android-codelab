package com.sap.codelab.utils.extensions

/**
 * @return an empty String.
 */
fun String.Companion.empty(): String = ""

/**
 * Truncates this string to at most [max] characters. If the string is longer it is cut to [max]
 * characters with a trailing ellipsis. Used to show only the first part of a memo's text in the
 * location reminder notification.
 *
 * @param max - the maximum number of characters to keep (default 140).
 * @return the truncated string.
 */
fun String.truncate(max: Int = 140): String = if (length <= max) this else take(max).trimEnd() + "…"