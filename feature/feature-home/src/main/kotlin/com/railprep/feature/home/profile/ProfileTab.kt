package com.railprep.feature.home.profile

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railprep.core.design.tokens.Spacing
import com.railprep.core.design.tokens.TouchTarget
import com.railprep.feature.home.R
import com.railprep.feature.notifications.NotificationsOptInPrompt
import com.railprep.feature.notifications.hasPostNotificationsPermission
import com.railprep.feature.notifications.shouldPromptForNotifications

@Composable
fun ProfileTab(
    onSignedOut: () -> Unit,
    onOpenEdit: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenDiag: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showLanguagePicker by remember { mutableStateOf(false) }
    var showNotifPrompt by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.Lg, vertical = Spacing.Xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.Md),
    ) {
        ProfileHeader(
            displayName = state.displayName ?: state.email ?: "You",
            email = state.email,
            onAvatarLongPress = onOpenDiag,
        )

        Text(
            text = if (state.streakCurrent > 0)
                stringResource(R.string.profile_streak_label_fmt, state.streakCurrent, state.streakBest)
            else stringResource(R.string.profile_streak_none),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.tertiary,
        )

        ProfileRow(icon = Icons.Filled.Edit, label = stringResource(R.string.profile_edit), onClick = onOpenEdit)
        ProfileRow(icon = Icons.Filled.BookmarkBorder, label = stringResource(R.string.profile_bookmarks), onClick = onOpenBookmarks)
        ProfileRow(icon = Icons.Filled.Translate, label = stringResource(R.string.profile_language), onClick = { showLanguagePicker = true })

        NotificationsToggleRow(
            enabled = state.notificationsEnabled,
            onToggle = { checked ->
                if (!checked) {
                    viewModel.setNotificationsEnabled(false)
                } else if (!shouldPromptForNotifications(context) &&
                           hasPostNotificationsPermission(context)) {
                    // Pre-13 or already granted — flip straight to ON.
                    viewModel.setNotificationsEnabled(true)
                } else {
                    // Android 13+ with no permission — show the dialog; it owns the system request.
                    showNotifPrompt = true
                }
            },
        )

        ProfileRow(icon = Icons.Filled.Info, label = stringResource(R.string.profile_about), onClick = onOpenAbout)

        Spacer(Modifier.size(Spacing.Md))

        ProfileRow(
            icon = Icons.Filled.Logout,
            label = stringResource(R.string.profile_sign_out),
            onClick = { viewModel.signOut(onSignedOut) },
            destructive = true,
        )

        Text(
            text = stringResource(R.string.profile_diag_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.Md),
        )
    }

    if (showLanguagePicker) {
        LanguagePickerDialog(
            current = state.language,
            onDismiss = { showLanguagePicker = false },
            onPick = { lang ->
                viewModel.setLanguage(lang)
                showLanguagePicker = false
            },
        )
    }

    NotificationsOptInPrompt(
        visible = showNotifPrompt,
        onAccepted = {
            showNotifPrompt = false
            viewModel.setNotificationsEnabled(true)
        },
        onDismissed = { showNotifPrompt = false },
    )
}

@Composable
private fun NotificationsToggleRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth().heightIn(min = TouchTarget.Min),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.Md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.size(Spacing.Md))
            Text(
                text = stringResource(com.railprep.feature.notifications.R.string.notif_toggle_label),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun ProfileHeader(
    displayName: String,
    email: String?,
    onAvatarLongPress: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Md),
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { onAvatarLongPress() })
                },
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp),
                    )
                }
            }
        }
        Column {
            Text(
                text = displayName,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = email ?: stringResource(R.string.profile_no_email),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProfileRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    destructive: Boolean = false,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = TouchTarget.Min),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Spacing.Md, vertical = Spacing.Md),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (destructive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.size(Spacing.Md))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (destructive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (!destructive) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LanguagePickerDialog(
    current: String,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.language_picker_title)) },
        text = {
            Column {
                LanguageChoice(
                    label = stringResource(R.string.language_picker_en),
                    selected = current == "en",
                    onClick = { onPick("en") },
                )
                LanguageChoice(
                    label = stringResource(R.string.language_picker_hi),
                    selected = current == "hi",
                    onClick = { onPick("hi") },
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } },
    )
}

@Composable
private fun LanguageChoice(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = TouchTarget.Min),
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, modifier = Modifier.weight(1f))
    }
}
