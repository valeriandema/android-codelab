package com.sap.codelab.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sap.codelab.R
import com.sap.codelab.domain.model.Memo
import com.sap.codelab.domain.notification.MemoReminder
import com.sap.codelab.MainActivity
import com.sap.codelab.EXTRA_MEMO_ID
import com.sap.codelab.utils.extensions.truncate
import com.sap.codelab.utils.permissions.RuntimePermissionHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val CHANNEL_ID: String = "memo_location_reminders"

/** Number of characters of the memo text shown in the notification, as required by the spec. */
private const val NOTIFICATION_TEXT_LENGTH: Int = 140

/**
 * Builds and shows the location-based reminder notifications. The application [Context] is injected
 * by Hilt.
 */
@Singleton
internal class Notifier @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : MemoReminder {

    /**
     * Creates the notification channel used for location reminders. Safe to call repeatedly;
     * should be called once on app startup. The channel is required on Android 8.0+ (API 26),
     * and the app's minSdk is 27 so it is always created.
     */
    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        NotificationManagerCompat.from(context).createNotificationChannel(channel)
    }

    /**
     * Shows a status-bar notification for the given memo, containing its title and the first
     * [NOTIFICATION_TEXT_LENGTH] characters of its text. Tapping the notification opens the memo.
     *
     * On Android 13+ (API 33) this silently does nothing if the POST_NOTIFICATIONS permission has
     * not been granted; the permission is requested when the memo is created.
     *
     * @param memo - the memo whose location was reached.
     */
    // The POST_NOTIFICATIONS permission is guarded by RuntimePermissionHelper.canPostNotifications()
    // below, but lint's MissingPermission inspection cannot follow that check across the call
    // boundary, so it is suppressed here.
    @SuppressLint("MissingPermission")
    override fun showMemoReminder(memo: Memo) {
        if (!RuntimePermissionHelper.canPostNotifications(context)) {
            return
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_MEMO_ID, memo.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            memo.id.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = memo.description.truncate(NOTIFICATION_TEXT_LENGTH)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(memo.title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(memo.id.toInt(), notification)
    }
}
