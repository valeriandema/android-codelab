package com.sap.codelab.utils.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.sap.codelab.R

/**
 * Drives a runtime-permission request for the given [permissions].
 *
 * When [rationaleTitle] and [rationaleMessage] are supplied, an explanatory dialog is shown before
 * the system permission prompt; the request is only launched if the user chooses to continue.
 *
 * The launcher is registered in [init], so an instance must be created while the host activity is
 * being created (e.g. as a field, or in onCreate before the activity is started).
 *
 * @param activity        - the host activity.
 * @param permissions     - the permissions to request.
 * @param rationaleTitle  - optional title for the rationale dialog.
 * @param rationaleMessage- optional message for the rationale dialog.
 * @param onGranted       - invoked once every requested permission is granted.
 */
internal class RuntimePermissionHelper(
    private val activity: AppCompatActivity,
    private val permissions: Array<String>,
    @param:StringRes private val rationaleTitle: Int? = null,
    @param:StringRes private val rationaleMessage: Int? = null,
    private val onGranted: () -> Unit = {},
) {

    private val launcher: ActivityResultLauncher<Array<String>> =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (permissions.all { result[it] == true || hasPermission(it) }) {
                onGranted()
            }
        }

    /**
     * Ensures the permissions are in place. If they are all already granted, [onGranted] is invoked
     * immediately; otherwise the rationale dialog (when configured) and then the system permission
     * dialog(s) are shown.
     */
    fun ensurePermissions() {
        if (permissions.all { hasPermission(it) }) {
            onGranted()
            return
        }
        if (rationaleTitle != null && rationaleMessage != null) {
            showRationale(rationaleTitle, rationaleMessage)
        } else {
            launcher.launch(permissions)
        }
    }

    private fun showRationale(@StringRes title: Int, @StringRes message: Int) {
        AlertDialog.Builder(activity)
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton(R.string.permission_rationale_not_now, null)
            .setPositiveButton(R.string.permission_rationale_continue) { _, _ ->
                launcher.launch(permissions)
            }
            .show()
    }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED

    companion object {

        /** @return true if [permission] is granted. */
        fun hasPermission(context: Context, permission: String): Boolean =
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

        /**
         * @return true if the app may post notifications. On Android 13+ (API 33) this requires the
         * POST_NOTIFICATIONS runtime permission; on earlier versions notifications are always allowed.
         */
        fun canPostNotifications(context: Context): Boolean =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    hasPermission(context, Manifest.permission.POST_NOTIFICATIONS)
    }
}
