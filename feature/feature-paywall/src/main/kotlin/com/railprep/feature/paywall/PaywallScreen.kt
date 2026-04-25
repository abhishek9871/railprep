package com.railprep.feature.paywall

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.railprep.core.design.tokens.Radius
import com.railprep.core.design.tokens.Spacing
import com.railprep.core.design.tokens.TouchTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    onBack: () -> Unit,
    viewModel: PaywallViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current.findActivity()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.paywall_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(Spacing.Lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.Md),
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(Radius.Md),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(Spacing.Lg), verticalArrangement = Arrangement.spacedBy(Spacing.Sm)) {
                        Icon(
                            Icons.Filled.LockOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(36.dp),
                        )
                        Text(
                            stringResource(R.string.paywall_headline),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            stringResource(R.string.paywall_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
            if (state.loading) {
                item { Box(Modifier.fillMaxWidth().padding(Spacing.Lg), Alignment.Center) { CircularProgressIndicator() } }
            }
            items(state.plans, key = { it.productId }) { plan ->
                PlanRow(
                    plan = plan,
                    activity = activity,
                    onBuy = viewModel::buy,
                )
            }
            item {
                state.message?.let { message ->
                    Text(
                        text = paywallMessage(message),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (message == "owned") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.size(Spacing.Sm))
                OutlinedButton(
                    onClick = viewModel::refresh,
                    modifier = Modifier.fillMaxWidth().heightIn(min = TouchTarget.Min),
                ) { Text(stringResource(R.string.paywall_refresh)) }
            }
        }
    }
}

@Composable
private fun PlanRow(
    plan: PaywallPlan,
    activity: Activity?,
    onBuy: (Activity, String) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(Radius.Md),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.Md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.Md),
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (plan.productId.contains("quarterly")) {
                        stringResource(R.string.paywall_quarterly)
                    } else {
                        stringResource(R.string.paywall_monthly)
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = plan.price,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Button(
                onClick = { activity?.let { onBuy(it, plan.productId) } },
                enabled = plan.available && activity != null,
            ) {
                Text(stringResource(R.string.paywall_subscribe))
            }
        }
    }
}

@Composable
private fun paywallMessage(code: String): String = when (code) {
    "owned" -> stringResource(R.string.paywall_owned)
    "unavailable" -> stringResource(R.string.paywall_unavailable)
    "cancelled" -> stringResource(R.string.paywall_cancelled)
    "setup_failed", "query_failed", "disconnected" -> stringResource(R.string.paywall_billing_error)
    else -> stringResource(R.string.paywall_purchase_failed)
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
