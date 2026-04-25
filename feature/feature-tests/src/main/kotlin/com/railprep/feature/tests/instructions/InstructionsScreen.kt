package com.railprep.feature.tests.instructions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railprep.core.design.tokens.Radius
import com.railprep.core.design.tokens.Spacing
import com.railprep.domain.model.Test
import com.railprep.domain.model.TestSection
import com.railprep.feature.tests.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructionsScreen(
    testId: String,
    onStartAttempt: (attemptId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: InstructionsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    var showBattOpt by remember { mutableStateOf(false) }

    LaunchedEffect(testId) { viewModel.load(testId) }

    LaunchedEffect(state.startedAttemptId) {
        state.startedAttemptId?.let { aid ->
            viewModel.clearStarted()
            onStartAttempt(aid)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.instructions_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        when {
            state.loading -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            state.test == null -> Box(
                Modifier.fillMaxSize().padding(innerPadding).padding(Spacing.Lg),
                contentAlignment = Alignment.Center,
            ) { Text(stringResource(R.string.player_load_error)) }

            else -> InstructionsBody(
                test = state.test!!,
                sections = state.sections,
                hasActiveAttempt = state.hasActiveAttempt,
                starting = state.starting,
                error = state.error,
                onStart = {
                    // Battery-optimization exemption prompt — only if not already whitelisted.
                    // Prompted at test Start (not app launch — launch-time prompts get denied).
                    if (needsBatteryOptExempt(ctx)) {
                        showBattOpt = true
                    } else {
                        viewModel.start()
                    }
                },
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
        }
    }

    if (showBattOpt) {
        val manufacturerName = Build.MANUFACTURER.takeIf { it.isNotBlank() }
            ?: stringResource(R.string.battopt_device_generic)
        AlertDialog(
            onDismissRequest = { showBattOpt = false; viewModel.start() },
            title = { Text(stringResource(R.string.battopt_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.battopt_body_fmt,
                        manufacturerName,
                        stringResource(batteryOptGuidanceRes()),
                    ),
                )
            },
            confirmButton = {
                Button(onClick = {
                    showBattOpt = false
                    openBatteryOptSettings(ctx)
                    viewModel.start()
                }) { Text(stringResource(R.string.battopt_open_settings)) }
            },
            dismissButton = {
                TextButton(onClick = { showBattOpt = false; viewModel.start() }) {
                    Text(stringResource(R.string.battopt_skip))
                }
            },
        )
    }
}

@Composable
private fun InstructionsBody(
    test: Test,
    sections: List<TestSection>,
    hasActiveAttempt: Boolean,
    starting: Boolean,
    error: String?,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    Column(
        modifier = modifier.verticalScroll(scroll).padding(PaddingValues(Spacing.Lg)),
        verticalArrangement = Arrangement.spacedBy(Spacing.Md),
    ) {
        Text(
            stringResource(R.string.instructions_header_fmt, test.titleEn),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (!test.titleHi.isNullOrBlank()) {
            Text(
                test.titleHi!!,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(Radius.Md),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(Spacing.Md),
                   verticalArrangement = Arrangement.spacedBy(Spacing.Xs)) {
                Text(stringResource(R.string.instructions_duration_fmt, test.totalMinutes),
                     style = MaterialTheme.typography.bodyLarge)
                Text(stringResource(R.string.instructions_qcount_fmt, test.totalQuestions),
                     style = MaterialTheme.typography.bodyLarge)
                Text(
                    stringResource(
                        R.string.instructions_neg_fmt,
                        "%.3f".format(test.negativeMarkingFraction),
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        Text(
            stringResource(R.string.instructions_sections_header),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        sections.forEach { s ->
            Text(
                stringResource(R.string.instructions_section_line_fmt, s.titleEn, s.questionCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.size(Spacing.Sm))
        Text(
            stringResource(R.string.instructions_rules_header),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        )
        Text(stringResource(R.string.instructions_rule_bilingual),
             style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.instructions_rule_timer),
             style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.instructions_rule_neg),
             style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.instructions_rule_submit),
             style = MaterialTheme.typography.bodyMedium)

        Spacer(Modifier.size(Spacing.Md))

        Button(
            onClick = onStart,
            enabled = !starting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (starting) stringResource(R.string.instructions_starting)
                else if (hasActiveAttempt) stringResource(R.string.instructions_resume)
                else stringResource(R.string.instructions_start),
            )
        }
        if (error == "start" || error == "pro") {
            Text(
                stringResource(
                    if (error == "pro") R.string.instructions_pro_required
                    else R.string.instructions_start_error,
                ),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@StringRes
private fun batteryOptGuidanceRes(): Int {
    val manufacturer = Build.MANUFACTURER.lowercase()
    return when {
        manufacturer.contains("oppo") || manufacturer.contains("realme") || manufacturer.contains("oneplus") ->
            R.string.battopt_oem_coloros
        manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") ->
            R.string.battopt_oem_miui
        manufacturer.contains("samsung") ->
            R.string.battopt_oem_oneui
        else -> R.string.battopt_oem_generic
    }
}

private fun needsBatteryOptExempt(ctx: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
    val pm = ctx.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
    return !pm.isIgnoringBatteryOptimizations(ctx.packageName)
}

private fun openBatteryOptSettings(ctx: Context) {
    // ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS requires the REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
    // permission which we don't declare — instead deep-link into the system settings so the user
    // can toggle it themselves. This is Play-policy safe.
    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    runCatching { ctx.startActivity(intent) }
        .onFailure {
            // Fall back to the app's app-info screen on OEMs that don't expose the settings activity.
            val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", ctx.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            runCatching { ctx.startActivity(fallback) }
        }
}
