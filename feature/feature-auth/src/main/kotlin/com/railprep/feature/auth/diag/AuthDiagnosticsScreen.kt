package com.railprep.feature.auth.diag

import android.accounts.AccountManager
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.credentials.CredentialManager
import com.railprep.core.design.RailPrepTheme
import com.railprep.core.design.tokens.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthDiagnosticsScreen(
    webClientId: String,
    supabaseUrl: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val info = remember {
        buildDiagnosticsSnapshot(
            context = context,
            webClientId = webClientId,
            supabaseUrl = supabaseUrl,
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Auth diagnostics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.Lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.Sm),
        ) {
            DiagRow("Supabase URL host", info.supabaseHost)
            DiagRow("Google Web Client ID (tail)", info.webClientIdTail)
            HorizontalDivider()
            DiagRow("Play Services installed", info.playServicesInstalled.yesNo())
            DiagRow("Play Services version", info.playServicesVersion ?: "—")
            DiagRow("Play Services package", info.playServicesPackageName ?: "—")
            HorizontalDivider()
            DiagRow("Google accounts on device", info.googleAccountCount.toString())
            DiagRow("Credential Manager available", info.credentialManagerAvailable.yesNo())
            info.credentialManagerError?.let { DiagRow("Credential Manager error", it) }
            HorizontalDivider()
            Text(
                "These values are captured once when the screen opens. Long-press the title again if you need a fresh snapshot.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DiagRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = Spacing.Md),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

private fun Boolean.yesNo(): String = if (this) "yes" else "no"

internal data class DiagnosticsSnapshot(
    val supabaseHost: String,
    val webClientIdTail: String,
    val playServicesInstalled: Boolean,
    val playServicesVersion: String?,
    val playServicesPackageName: String?,
    val googleAccountCount: Int,
    val credentialManagerAvailable: Boolean,
    val credentialManagerError: String?,
)

internal fun buildDiagnosticsSnapshot(
    context: android.content.Context,
    webClientId: String,
    supabaseUrl: String,
): DiagnosticsSnapshot {
    // Supabase host extraction — tolerate malformed URLs.
    val supabaseHost = runCatching {
        android.net.Uri.parse(supabaseUrl).host ?: "—"
    }.getOrElse { "invalid" }

    // Web client ID tail (last 12 chars, then obfuscate middle of the full ID).
    val webClientIdTail = if (webClientId.length >= 12) {
        "…${webClientId.takeLast(12)}"
    } else {
        "(empty)"
    }

    // Play Services lookup via PackageManager — avoids a new Google API dep.
    val (playInstalled, playVersion, playPackage) = runCatching {
        val pm = context.packageManager
        val pkg = pm.getPackageInfo("com.google.android.gms", 0)
        @Suppress("DEPRECATION")
        Triple(true, "${pkg.versionName} (code=${pkg.longVersionCode})", pkg.packageName)
    }.getOrElse { t ->
        if (t is PackageManager.NameNotFoundException) Triple(false, null, null)
        else Triple(false, null, "query failed: ${t.javaClass.simpleName}")
    }

    // Google accounts on device.
    val googleAccountCount = runCatching {
        AccountManager.get(context).getAccountsByType("com.google").size
    }.getOrDefault(-1)

    // Credential Manager availability — create + a GOne-check probe.
    val (cmAvailable, cmError) = runCatching {
        CredentialManager.create(context)
        true to null
    }.getOrElse { t ->
        false to "${t.javaClass.simpleName}: ${t.message}"
    } as Pair<Boolean, String?>

    return DiagnosticsSnapshot(
        supabaseHost = supabaseHost,
        webClientIdTail = webClientIdTail,
        playServicesInstalled = playInstalled,
        playServicesVersion = playVersion,
        playServicesPackageName = playPackage,
        googleAccountCount = googleAccountCount,
        credentialManagerAvailable = cmAvailable,
        credentialManagerError = cmError,
    )
}

@Preview(widthDp = 360, heightDp = 780)
@Composable
private fun DiagnosticsPreview() {
    RailPrepTheme {
        AuthDiagnosticsScreen(
            webClientId = "123456789-abcdefghijk.apps.googleusercontent.com",
            supabaseUrl = "https://example.supabase.co",
            onBack = {},
        )
    }
}
