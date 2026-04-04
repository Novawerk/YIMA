package com.haodong.yimalaile.ui.pages.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

@Composable
fun SettingsScreen(
    currentDarkMode: String,
    currentLanguage: String?,
    onDarkModeChange: (String) -> Unit,
    onLanguageChange: (String?) -> Unit,
    onBack: () -> Unit,
    onClearData: () -> Unit,
) {
    var showClearConfirm by remember { mutableStateOf(false) }

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
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.weight(1f))
            Text(stringResource(Res.string.settings_title), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(40.dp))
        }

        Spacer(Modifier.height(32.dp))

        // ---------- Dark mode ----------
        SectionLabel(stringResource(Res.string.settings_display_mode))
        Spacer(Modifier.height(12.dp))
        val darkModes = listOf("system" to Res.string.settings_mode_system, "light" to Res.string.settings_mode_light, "dark" to Res.string.settings_mode_dark)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            darkModes.forEachIndexed { index, (value, labelRes) ->
                SegmentedButton(
                    selected = currentDarkMode == value,
                    onClick = { onDarkModeChange(value) },
                    shape = SegmentedButtonDefaults.itemShape(index, darkModes.size),
                ) { Text(stringResource(labelRes)) }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ---------- Language ----------
        SectionLabel(stringResource(Res.string.settings_language))
        Spacer(Modifier.height(12.dp))
        val langs = listOf(null to Res.string.settings_language_follow_system, "en" to Res.string.settings_language_en, "zh" to Res.string.settings_language_zh)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            langs.forEachIndexed { index, (value, labelRes) ->
                SegmentedButton(
                    selected = currentLanguage == value,
                    onClick = { onLanguageChange(value) },
                    shape = SegmentedButtonDefaults.itemShape(index, langs.size),
                ) { Text(stringResource(labelRes)) }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ---------- Other settings ----------
        SettingsItem(label = stringResource(Res.string.settings_clear_data), value = stringResource(Res.string.settings_clear_data_value), onClick = { showClearConfirm = true }, destructive = true)
        Spacer(Modifier.height(12.dp))
        SettingsItem(label = stringResource(Res.string.app_version), value = "1.0.0")

        Spacer(Modifier.height(32.dp))

        // About
        Box(
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .padding(24.dp),
        ) {
            Column {
                Text(
                    stringResource(Res.string.settings_about),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(Res.string.settings_about_org),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(Res.string.settings_about_motto),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

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
