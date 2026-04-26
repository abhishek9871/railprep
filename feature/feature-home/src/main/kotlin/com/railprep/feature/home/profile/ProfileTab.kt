package com.railprep.feature.home.profile

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railprep.core.design.tokens.Spacing
import com.railprep.core.design.tokens.TouchTarget
import com.railprep.core.design.theme.Line
import com.railprep.core.design.theme.Primary
import com.railprep.core.design.theme.PrimarySoft
import com.railprep.core.design.theme.Success
import com.railprep.core.design.theme.SuccessSoft
import com.railprep.core.design.theme.SurfaceWhite
import com.railprep.feature.home.R
import com.railprep.feature.notifications.NotificationsOptInPrompt
import com.railprep.feature.notifications.hasPostNotificationsPermission
import com.railprep.feature.notifications.shouldPromptForNotifications

@Composable
fun ProfileTab(
    onSignedOut: () -> Unit,
    onOpenEdit: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenSavedQuestions: () -> Unit,
    onOpenPerformance: () -> Unit,
    onOpenAccountSettings: () -> Unit,
    onOpenPro: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenDiag: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showLanguagePicker by remember { mutableStateOf(false) }
    var showNotifPrompt by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshBookmarkCounts()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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

        ProfileRow(
            icon = Icons.Filled.BookmarkBorder,
            label = stringResource(R.string.profile_bookmarks),
            badgeCount = state.topicBookmarkCount,
            onClick = onOpenBookmarks,
        )
        ProfileRow(
            icon = Icons.Filled.Quiz,
            label = stringResource(R.string.profile_saved_questions),
            badgeCount = state.savedQuestionCount,
            onClick = onOpenSavedQuestions,
        )
        ProfileRow(
            icon = Icons.Filled.QueryStats,
            label = stringResource(R.string.profile_performance),
            onClick = onOpenPerformance,
        )
        ProfileRow(
            icon = Icons.Filled.LockOpen,
            label = stringResource(R.string.profile_pro),
            onClick = onOpenPro,
        )
        ProfileRow(icon = Icons.Filled.Edit, label = stringResource(R.string.profile_edit), onClick = onOpenEdit)
        ProfileRow(
            icon = Icons.Filled.Security,
            label = stringResource(R.string.profile_account_settings),
            onClick = onOpenAccountSettings,
        )
        ProfileRow(
            icon = Icons.AutoMirrored.Filled.Logout,
            label = stringResource(R.string.profile_sign_out),
            onClick = { showSignOutConfirm = true },
            destructive = true,
        )
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
                showLanguagePicker = false
                viewModel.setLanguage(lang) {
                    context.findActivity()?.takeUnless { it.isFinishing || it.isDestroyed }?.recreate()
                }
            },
        )
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text(stringResource(R.string.profile_sign_out_confirm_title)) },
            text = { Text(stringResource(R.string.profile_sign_out_confirm_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutConfirm = false
                        viewModel.signOut(onSignedOut)
                    },
                ) {
                    Text(
                        text = stringResource(R.string.profile_sign_out_confirm_ok),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text(stringResource(R.string.profile_sign_out_confirm_cancel))
                }
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
    badgeCount: Int? = null,
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
            if (badgeCount != null) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = badgeCount.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = Spacing.Sm, vertical = 2.dp),
                    )
                }
                Spacer(Modifier.size(Spacing.Sm))
            }
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
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.Sm)) {
                LanguageChoice(
                    label = stringResource(R.string.language_picker_en),
                    subtitle = stringResource(R.string.language_picker_en_subtitle),
                    code = "EN",
                    selected = current == "en",
                    onClick = { onPick("en") },
                )
                LanguageChoice(
                    label = stringResource(R.string.language_picker_hi),
                    subtitle = stringResource(R.string.language_picker_hi_subtitle),
                    code = "HI",
                    selected = current == "hi",
                    onClick = { onPick("hi") },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.language_picker_cancel))
            }
        },
    )
}

@Composable
private fun LanguageChoice(
    label: String,
    subtitle: String,
    code: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val border = if (selected) Primary else Line
    val background = if (selected) PrimarySoft else SurfaceWhite
    Surface(
        color = background,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(if (selected) 2.dp else 1.dp, border),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.Md),
            modifier = Modifier.padding(Spacing.Md),
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Primary,
                modifier = Modifier.size(44.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (selected) {
                Surface(
                    shape = CircleShape,
                    color = SuccessSoft,
                    modifier = Modifier.size(30.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Success,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
