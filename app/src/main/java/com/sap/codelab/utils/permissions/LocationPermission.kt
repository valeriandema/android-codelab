package com.sap.codelab.utils.permissions

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.sap.codelab.R

internal fun interface LocationPermissionController {
    fun ensurePermissions()
}

@Composable
internal fun rememberLocationPermissionController(
    onGranted: () -> Unit = {},
): LocationPermissionController {
    val context = LocalContext.current
    val permissions = remember { locationReminderPermissions() }
    val currentOnGranted by rememberUpdatedState(onGranted)
    var showRationale by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        if (permissions.all { result[it] == true || RuntimePermissionHelper.hasPermission(context, it) }) {
            currentOnGranted()
        }
    }

    val controller = remember(context) {
        LocationPermissionController {
            if (permissions.all { RuntimePermissionHelper.hasPermission(context, it) }) {
                currentOnGranted()
            } else {
                showRationale = true
            }
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text(stringResource(R.string.permission_location_title)) },
            text = { Text(stringResource(R.string.permission_location_rationale)) },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    launcher.launch(permissions)
                }) { Text(stringResource(R.string.permission_rationale_continue)) }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) {
                    Text(stringResource(R.string.permission_rationale_not_now))
                }
            },
        )
    }

    return controller
}
