package com.haodong.yimalaile.ui.pages.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.domain.health.HealthSyncManager
import com.haodong.yimalaile.ui.components.PrimaryCta
import com.haodong.yimalaile.ui.components.SmallSpacer
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.onboarding_back
import yimalaile.composeapp.generated.resources.onboarding_continue
import yimalaile.composeapp.generated.resources.onboarding_health_connect
import yimalaile.composeapp.generated.resources.onboarding_health_desc
import yimalaile.composeapp.generated.resources.onboarding_health_failed
import yimalaile.composeapp.generated.resources.onboarding_health_imported
import yimalaile.composeapp.generated.resources.onboarding_health_imported_one
import yimalaile.composeapp.generated.resources.onboarding_health_no_records
import yimalaile.composeapp.generated.resources.onboarding_health_retry
import yimalaile.composeapp.generated.resources.onboarding_health_syncing
import yimalaile.composeapp.generated.resources.onboarding_health_title
import yimalaile.composeapp.generated.resources.onboarding_skip

private sealed interface HealthSyncUi {
    data object Idle : HealthSyncUi
    data object Syncing : HealthSyncUi
    data class Done(val imported: Int) : HealthSyncUi
    data object NoRecords : HealthSyncUi
    data object Failed : HealthSyncUi
}

@Composable
internal fun HealthSyncStep(
    healthSyncManager: HealthSyncManager,
    onConnected: (imported: Int) -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var uiState by remember { mutableStateOf<HealthSyncUi>(HealthSyncUi.Idle) }

    fun runConnect() {
        if (uiState == HealthSyncUi.Syncing) return
        scope.launch {
            uiState = HealthSyncUi.Syncing
            val authorized = runCatching { healthSyncManager.requestAuthorization() }
                .getOrDefault(false)
            if (!authorized) {
                uiState = HealthSyncUi.Failed
                return@launch
            }
            val result = runCatching { healthSyncManager.sync() }.getOrNull()
            uiState = when {
                result == null -> HealthSyncUi.Failed
                result.imported == 0 -> HealthSyncUi.NoRecords
                else -> HealthSyncUi.Done(result.imported)
            }
        }
    }

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))
        FeatureIcon(icon = Icons.Outlined.HealthAndSafety, tint = MaterialTheme.colorScheme.primary)
        SmallSpacer(20)
        Text(
            stringResource(Res.string.onboarding_health_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        SmallSpacer(12)
        Text(
            stringResource(Res.string.onboarding_health_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        Spacer(Modifier.weight(1f))

        when (val state = uiState) {
            HealthSyncUi.Syncing -> StatusBanner(
                icon = null,
                text = stringResource(Res.string.onboarding_health_syncing),
                tint = MaterialTheme.colorScheme.primary,
                showSpinner = true,
            )
            is HealthSyncUi.Done -> StatusBanner(
                icon = Icons.Outlined.CheckCircle,
                text = if (state.imported == 1)
                    stringResource(Res.string.onboarding_health_imported_one)
                else
                    stringResource(Res.string.onboarding_health_imported, state.imported),
                tint = MaterialTheme.colorScheme.primary,
            )
            HealthSyncUi.NoRecords -> StatusBanner(
                icon = Icons.Outlined.Info,
                text = stringResource(Res.string.onboarding_health_no_records),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HealthSyncUi.Failed -> StatusBanner(
                icon = Icons.Outlined.Info,
                text = stringResource(Res.string.onboarding_health_failed),
                tint = MaterialTheme.colorScheme.error,
            )
            HealthSyncUi.Idle -> {}
        }

        SmallSpacer(20)

        when (val state = uiState) {
            is HealthSyncUi.Done -> {
                PrimaryCta(
                    text = stringResource(Res.string.onboarding_continue),
                    onClick = { onConnected(state.imported) },
                )
            }
            HealthSyncUi.NoRecords -> {
                PrimaryCta(
                    text = stringResource(Res.string.onboarding_continue),
                    onClick = onSkip,
                )
            }
            HealthSyncUi.Failed -> {
                PrimaryCta(
                    text = stringResource(Res.string.onboarding_health_retry),
                    onClick = { runConnect() },
                )
            }
            else -> {
                PrimaryCta(
                    text = stringResource(Res.string.onboarding_health_connect),
                    onClick = { runConnect() },
                    enabled = uiState != HealthSyncUi.Syncing,
                )
            }
        }

        SmallSpacer(8)

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack, enabled = uiState != HealthSyncUi.Syncing) {
                Text(stringResource(Res.string.onboarding_back))
            }
            if (uiState !is HealthSyncUi.Done) {
                TextButton(onClick = onSkip, enabled = uiState != HealthSyncUi.Syncing) {
                    Text(stringResource(Res.string.onboarding_skip))
                }
            } else {
                Spacer(Modifier.size(0.dp))
            }
        }
    }
}
