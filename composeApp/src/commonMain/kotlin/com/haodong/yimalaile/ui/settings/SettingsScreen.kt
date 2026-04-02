package com.haodong.yimalaile.ui.settings

import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.haodong.yimalaile.ui.theme.AppColors
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.app_version
import yimalaile.composeapp.generated.resources.disclaimer_view_again
import yimalaile.composeapp.generated.resources.settings_language
import yimalaile.composeapp.generated.resources.settings_language_follow_system
import yimalaile.composeapp.generated.resources.settings_privacy
import yimalaile.composeapp.generated.resources.settings_title

@Composable
fun SettingsScreen(onBack: () -> Unit) {
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
            Box(Modifier.size(40.dp)) // balance
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
            label = stringResource(Res.string.app_version),
            value = "1.0.0",
        )
    }
}

@Composable
private fun SettingsItem(label: String, value: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.BlushPink.copy(alpha = 0.4f))
            .padding(20.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = AppColors.DarkCoffee)
            Spacer(Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.bodyMedium, color = AppColors.DarkCoffee.copy(alpha = 0.5f))
        }
    }
}
