package com.sap.codelab.domain.notification

import com.sap.codelab.domain.model.Memo

/**
 * Port for posting a location reminder notification for a memo. Implemented by the app layer so the
 * geofence module can notify the user without depending on the notification UI.
 */
interface MemoReminder {
    fun showMemoReminder(memo: Memo)
}
