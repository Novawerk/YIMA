package com.haodong.yimalaile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haodong.yimalaile.ui.theme.AppColors
import com.haodong.yimalaile.ui.theme.AppShapes
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.*

/**
 * Phase 1 Settings: Hand-drawn style settings screen inspired by Stitch designs.
 */
@Composable
fun SettingsScreen(
    onClose: () -> Unit
) {
    val showDisclaimer = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BackgroundLight)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(48.dp)
                    .background(AppColors.Accent.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(Res.string.nav_back), tint = AppColors.Primary)
            }
            Text(
                text = stringResource(Res.string.settings_title),
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Primary
                )
            )
            Spacer(modifier = Modifier.size(48.dp)) // Placeholder for symmetry
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Avatar / Profile Section (Hand-drawn style)
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(AppShapes.HeroBlob)
                .background(AppColors.Primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = AppColors.Primary.copy(alpha = 0.6f)
            )
        }

        Text(
            text = stringResource(Res.string.settings_profile),
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.Primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Settings Items
        SettingsItem(
            icon = Icons.Default.Timer,
            title = stringResource(Res.string.settings_period_length),
            value = stringResource(Res.string.settings_period_length_value),
            color = AppColors.CardBg1
        )

        SettingsItem(
            icon = Icons.Default.Autorenew,
            title = stringResource(Res.string.settings_cycle_length),
            value = stringResource(Res.string.settings_cycle_length_value),
            color = AppColors.CardBg2
        )

        SettingsItem(
            icon = Icons.Default.Notifications,
            title = stringResource(Res.string.settings_reminders),
            value = stringResource(Res.string.settings_reminders_on),
            color = AppColors.CardBg1
        )

        SettingsItem(
            icon = Icons.Default.Security,
            title = stringResource(Res.string.settings_privacy),
            value = "",
            color = AppColors.CardBg2,
            onClick = { showDisclaimer.value = true }
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = stringResource(Res.string.app_version),
            style = TextStyle(
                fontSize = 14.sp,
                color = AppColors.Primary.copy(alpha = 0.4f)
            )
        )

        if (showDisclaimer.value) {
            PrivacyDisclaimerPlaceholder(
                onAccept = { showDisclaimer.value = false }
            )
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    value: String,
    color: Color,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(24.dp),
        color = color.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(AppColors.BackgroundLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.Primary
                ),
                modifier = Modifier.weight(1f)
            )

            if (value.isNotEmpty()) {
                Text(
                    text = value,
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = AppColors.Primary.copy(alpha = 0.6f)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AppColors.Primary.copy(alpha = 0.3f)
            )
        }
    }
}
