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

@Composable
private fun SliderDialog(
    title: String,
    currentValue: Int,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    minLabel: String,
    maxLabel: String,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var sliderValue by remember { mutableStateOf(currentValue.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "${sliderValue.toInt()}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(Res.string.unit_days),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(16.dp))
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = valueRange,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(minLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(maxLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(sliderValue.toInt()) }) {
                Text(stringResource(Res.string.dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.dialog_cancel))
            }
        },
    )
}

// ════════════════════════════════════════════════════════════════
// Inline toggle with icons (like the home bottom bar)
// ════════════════════════════════════════════════════════════════

private data class ToggleItem(val value: String, val label: String, val icon: ImageVector)

@Composable
private fun InlineIconToggle(
    items: List<ToggleItem>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(50),
    ) {
        Row(
            Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val isSelected = item.value == selected
                Surface(
                    onClick = { onSelect(item.value) },
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f).height(40.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(item.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(item.label, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Inline text toggle (compact labels)
// ════════════════════════════════════════════════════════════════

@Composable
private fun <T> InlineTextToggle(
    items: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(50),
    ) {
        Row(
            Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { (value, label) ->
                val isSelected = value == selected
                Surface(
                    onClick = { onSelect(value) },
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f).height(40.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════════
// Shared components
// ════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(Res.string.settings_about_org),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                stringResource(Res.string.settings_about_motto),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            HorizontalDivider()
            Text(stringResource(Res.string.about_team_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(stringResource(Res.string.about_team_members), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(stringResource(Res.string.about_links_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(stringResource(Res.string.about_github), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
}

@Composable
private fun SettingsItem(label: String, value: String, onClick: (() -> Unit)? = null, destructive: Boolean = false) {
    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (destructive) MaterialTheme.colorScheme.error.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(20.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
