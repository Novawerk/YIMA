package com.haodong.yimalaile.ui.settings

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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.ui.theme.AppColors
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.*

@Composable
fun SettingsScreen(
    currentPalette: String,
    currentDarkMode: String,
    onPaletteChange: (String) -> Unit,
    onDarkModeChange: (String) -> Unit,
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

        // ---------- Color palette ----------
        SectionLabel(stringResource(Res.string.settings_color_palette))
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PaletteCard(
                name = stringResource(Res.string.settings_palette_warm),
                colors = listOf(Color(0xFFa66d6d), Color(0xFFF7CDC3), Color(0xFFFFF9F8)),
                selected = currentPalette == "warm",
                onClick = { onPaletteChange("warm") },
                modifier = Modifier.weight(1f),
            )
            PaletteCard(
                name = stringResource(Res.string.settings_palette_vivid),
                colors = listOf(Color(0xFFE91E63), Color(0xFFFFEB3B), Color(0xFFFFFDE7)),
                selected = currentPalette == "vivid",
                onClick = { onPaletteChange("vivid") },
                modifier = Modifier.weight(1f),
            )
            PaletteCard(
                name = stringResource(Res.string.settings_palette_mono),
                colors = listOf(Color(0xFF333333), Color(0xFF9E9E9E), Color(0xFFFFFFFF)),
                selected = currentPalette == "mono",
                onClick = { onPaletteChange("mono") },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(24.dp))

        // ---------- Dark mode ----------
        SectionLabel(stringResource(Res.string.settings_display_mode))
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ModeChip(stringResource(Res.string.settings_mode_system), selected = currentDarkMode == "system", onClick = { onDarkModeChange("system") }, modifier = Modifier.weight(1f))
            ModeChip(stringResource(Res.string.settings_mode_light), selected = currentDarkMode == "light", onClick = { onDarkModeChange("light") }, modifier = Modifier.weight(1f))
            ModeChip(stringResource(Res.string.settings_mode_dark), selected = currentDarkMode == "dark", onClick = { onDarkModeChange("dark") }, modifier = Modifier.weight(1f))
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
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
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
private fun PaletteCard(
    name: String,
    colors: List<Color>,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .then(
                if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                else Modifier
            )
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            colors.forEach { color ->
                Box(Modifier.size(20.dp).clip(CircleShape).background(color))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            name,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ModeChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (selected) Modifier.background(MaterialTheme.colorScheme.primary)
                else Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
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
