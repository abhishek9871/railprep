package com.railprep.feature.notifications

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat

/**
 * First-submit opt-in dialog for daily reminders. Called from DailyResultsScreen
 * on the user's first successful digest submission. NEVER called at app launch —
 * this is the only entry point for POST_NOTIFICATIONS to get requested.
 *
 * Contract with the caller:
 *   - [onAccepted] is invoked after the user taps "Turn on" AND grants system permission.
 *     Caller should flip profiles.notifications_enabled=true and call
 *     DigestReminderScheduler.enable().
 *   - [onDismissed] is invoked on "Not now", system denial, or pre-first-launch dismissal.
 *     Caller should persist the skip so we never prompt again.
 *   - Use [shouldShow] to decide whether to render this composable at all.
 */
@Composable
fun NotificationsOptInPrompt(
    visible: Boolean,
    onAccepted: () -> Unit,
    onDismissed: () -> Unit,
) {
    if (!visible) return
    val context = LocalContext.current
    var waitingOnSystem by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        waitingOnSystem = false
        if (granted) onAccepted() else onDismissed()
    }

    AlertDialog(
        onDismissRequest = { onDismissed() },
        title = { Text(stringResource(R.string.notif_optin_title)) },
        text = { Text(stringResource(R.string.notif_optin_body)) },
        confirmButton = {
            TextButton(onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val alreadyGranted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.POST_NOTIFICATIONS,
                    ) == PackageManager.PERMISSION_GRANTED
                    if (alreadyGranted) {
                        onAccepted()
                    } else {
                        waitingOnSystem = true
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {
                    // Pre-13 — runtime permission not required.
                    onAccepted()
                }
            }) {
                Text(stringResource(R.string.notif_optin_yes))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissed() }) {
                Text(stringResource(R.string.notif_optin_no))
            }
        },
    )
}

/** Cheap helper so callers don't have to know the SDK gate. */
fun shouldPromptForNotifications(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.POST_NOTIFICATIONS,
    ) != PackageManager.PERMISSION_GRANTED
}

/** `true` iff the system actively permits the app to post. Used in the Profile toggle. */
fun hasPostNotificationsPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED
}
