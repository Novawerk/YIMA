package com.haodong.yimalaile.ui.pages.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalUriHandler
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

@Composable
fun SettingsScreen(
    currentDarkMode: String,
    currentLanguage: String?,
    currentCycleLength: Int,
    currentPeriodDuration: Int,
    onDarkModeChange: (String) -> Unit,
    onLanguageChange: (String?) -> Unit,
    onCycleLengthChange: (Int) -> Unit,
    onPeriodDurationChange: (Int) -> Unit,
    onBack: () -> Unit,
    onClearData: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    var showClearConfirm by remember { mutableStateOf(false) }
    var showAboutSheet by remember { mutableStateOf(false) }
    var showPeriodDurationDialog by remember { mutableStateOf(false) }
    var showCycleLengthDialog by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 24.dp),
    ) {
        // Top bar
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.weight(1f))
            Text(stringResource(Res.string.settings_title), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(40.dp))
        }

        Spacer(Modifier.height(32.dp))

        // ── Display Mode — icon toggle ──
        SectionLabel(stringResource(Res.string.settings_display_mode))
        Spacer(Modifier.height(12.dp))
        InlineIconToggle(
            items = listOf(
                ToggleItem("system", stringResource(Res.string.settings_mode_system), Icons.Outlined.Smartphone),
                ToggleItem("light", stringResource(Res.string.settings_mode_light), Icons.Outlined.LightMode),
                ToggleItem("dark", stringResource(Res.string.settings_mode_dark), Icons.Outlined.DarkMode),
            ),
            selected = currentDarkMode,
            onSelect = onDarkModeChange,
        )

        Spacer(Modifier.height(24.dp))

        // ── Language — text toggle ──
        SectionLabel(stringResource(Res.string.settings_language))
        Spacer(Modifier.height(12.dp))
        InlineTextToggle(
            items = listOf(
                null to "Auto",
                "en" to "EN",
                "zh" to "中文",
            ),
            selected = currentLanguage,
            onSelect = onLanguageChange,
        )

        Spacer(Modifier.height(24.dp))

        // ── Period Duration — display only, tap to edit ──
        SettingsItem(
            label = stringResource(Res.string.settings_period_duration),
            value = "${currentPeriodDuration} ${stringResource(Res.string.unit_days)}",
            onClick = { showPeriodDurationDialog = true },
        )

        Spacer(Modifier.height(12.dp))

        // ── Cycle Length — display only, tap to edit ──
        SettingsItem(
            label = stringResource(Res.string.settings_cycle_length),
            value = "${currentCycleLength} ${stringResource(Res.string.unit_days)}",
            onClick = { showCycleLengthDialog = true },
        )

        Spacer(Modifier.height(48.dp))

        // ── About & version ──
        Surface(
            onClick = { showAboutSheet = true },
            tonalElevation = 1.dp,
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(Res.string.settings_about),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(Res.string.app_version),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            " · ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        )
                        Text(
                            stringResource(Res.string.settings_made_by),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text("→", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // ── Clear data ──
        Spacer(Modifier.height(12.dp))
        SettingsItem(
            label = stringResource(Res.string.settings_clear_data),
            value = stringResource(Res.string.settings_clear_data_value),
            onClick = { showClearConfirm = true },
            destructive = true,
        )

        // ── Review ──
        Spacer(Modifier.weight(1f))
        SettingsItem(
            label = stringResource(Res.string.settings_review),
            value = "→",
            onClick = { uriHandler.openUri("https://github.com/Novawerk/yimalaile") },
        )
        Spacer(Modifier.height(16.dp))
    }

    // ── Dialogs ──

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(stringResource(Res.string.settings_clear_title)) },
            text = { Text(stringResource(Res.string.settings_clear_body)) },
            confirmButton = {
                TextButton(onClick = { showClearConfirm = false; onClearData() }) {
                    Text(stringResource(Res.string.dialog_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(stringResource(Res.string.dialog_cancel))
                }
            },
        )
    }

    if (showAboutSheet) {
        AboutSheet(onDismiss = { showAboutSheet = false })
    }

    if (showPeriodDurationDialog) {
        SliderDialog(
            title = stringResource(Res.string.settings_period_duration),
            currentValue = currentPeriodDuration,
            valueRange = 2f..10f,
            steps = 7,
            minLabel = "2",
            maxLabel = "10",
            onConfirm = { onPeriodDurationChange(it); showPeriodDurationDialog = false },
            onDismiss = { showPeriodDurationDialog = false },
        )
    }

    if (showCycleLengthDialog) {
        SliderDialog(
            title = stringResource(Res.string.settings_cycle_length),
            currentValue = currentCycleLength,
            valueRange = 20f..45f,
            steps = 24,
            minLabel = "20",
            maxLabel = "45",
            onConfirm = { onCycleLengthChange(it); showCycleLengthDialog = false },
            onDismiss = { showCycleLengthDialog = false },
        )
    }
}

// ════════════════════════════════════════════════════════════════
// Slider dialog for editing numeric values
// ════════════════════════════════════════════════════════════════
