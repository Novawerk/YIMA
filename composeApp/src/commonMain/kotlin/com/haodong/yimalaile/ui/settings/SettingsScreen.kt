package com.haodong.yimalaile.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.ui.theme.AppColors
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.app_version
import yimalaile.composeapp.generated.resources.dialog_cancel
import yimalaile.composeapp.generated.resources.dialog_confirm
import yimalaile.composeapp.generated.resources.disclaimer_view_again
import yimalaile.composeapp.generated.resources.settings_language
import yimalaile.composeapp.generated.resources.settings_language_follow_system
import yimalaile.composeapp.generated.resources.settings_privacy
import yimalaile.composeapp.generated.resources.settings_title

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onClearData: () -> Unit,
) {
    var showClearConfirm by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp),
    ) {
        // Top bar
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AppColors.WarmPeach.copy(alpha = 0.5f)),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AppColors.DeepRose)
            }
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(Res.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
                color = AppColors.DarkCoffee,
            )
            Spacer(Modifier.weight(1f))
            Box(Modifier.size(40.dp))
        }

        Spacer(Modifier.height(32.dp))

        SettingsItem(
            label = stringResource(Res.string.settings_language),
            value = stringResource(Res.string.settings_language_follow_system),
        )
        Spacer(Modifier.height(12.dp))
        SettingsItem(
            label = stringResource(Res.string.settings_privacy),
            value = stringResource(Res.string.disclaimer_view_again),
        )
        Spacer(Modifier.height(12.dp))
        SettingsItem(
            label = "清除所有数据",
            value = "重新开始",
            onClick = { showClearConfirm = true },
            destructive = true,
        )
        Spacer(Modifier.height(12.dp))
        SettingsItem(
            label = stringResource(Res.string.app_version),
            value = "1.0.0",
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("确认清除") },
            text = { Text("这将删除所有经期记录并重新开始引导。此操作无法撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    showClearConfirm = false
                    onClearData()
                }) {
                    Text(
                        stringResource(Res.string.dialog_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
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
private fun SettingsItem(
    label: String,
    value: String,
    onClick: (() -> Unit)? = null,
    destructive: Boolean = false,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (destructive) MaterialTheme.colorScheme.error.copy(alpha = 0.08f)
                else AppColors.BlushPink.copy(alpha = 0.4f)
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(20.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (destructive) MaterialTheme.colorScheme.error else AppColors.DarkCoffee,
            )
            Spacer(Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.bodyMedium, color = AppColors.DarkCoffee.copy(alpha = 0.5f))
        }
    }
}
