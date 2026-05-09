package com.haodong.yimalaile.ui.pages.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.app_name
import yimalaile.composeapp.generated.resources.logo
import yimalaile.composeapp.generated.resources.splash_built_by

private const val LOGO_DURATION_MS = 700
private const val NAME_DURATION_MS = 450
private const val BRAND_DURATION_MS = 750
private const val BRAND_DELAY_MS = 600L
private const val MIN_VISIBLE_AFTER_BRAND_MS = 700L

/**
 * Brand splash. Logo + app name fade and lift in; the "Built by Novawerk"
 * tagline floats up from the bottom slightly later — a stage that the
 * studio's name slips onto without stealing the show.
 *
 * Stays visible until both the entrance animation has settled AND
 * upstream state ([ready]) is loaded, then calls [onFinish].
 */
@Composable
fun SplashScreen(
    ready: Boolean,
    onFinish: () -> Unit,
) {
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.86f) }
    val logoOffsetDp = remember { Animatable(12f) }
    val nameAlpha = remember { Animatable(0f) }
    val brandAlpha = remember { Animatable(0f) }
    val brandOffsetDp = remember { Animatable(40f) }

    LaunchedEffect(Unit) {
        launch { logoAlpha.animateTo(1f, tween(LOGO_DURATION_MS, easing = FastOutSlowInEasing)) }
        launch { logoScale.animateTo(1f, tween(LOGO_DURATION_MS + 100, easing = FastOutSlowInEasing)) }
        launch { logoOffsetDp.animateTo(0f, tween(LOGO_DURATION_MS, easing = FastOutSlowInEasing)) }
    }

    LaunchedEffect(Unit) {
        delay(LOGO_DURATION_MS / 2L)
        nameAlpha.animateTo(1f, tween(NAME_DURATION_MS, easing = FastOutSlowInEasing))
    }

    var brandSettled by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(BRAND_DELAY_MS)
        launch { brandAlpha.animateTo(1f, tween(BRAND_DURATION_MS, easing = FastOutSlowInEasing)) }
        brandOffsetDp.animateTo(0f, tween(BRAND_DURATION_MS, easing = FastOutSlowInEasing))
        delay(MIN_VISIBLE_AFTER_BRAND_MS)
        brandSettled = true
    }

    LaunchedEffect(brandSettled, ready) {
        if (brandSettled && ready) onFinish()
    }

    Surface(
        modifier = Modifier.fillMaxSize().testTag("splash"),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.logo),
                    contentDescription = null,
                    modifier = Modifier
                        .size(128.dp)
                        .offset(y = logoOffsetDp.value.dp)
                        .graphicsLayer {
                            alpha = logoAlpha.value
                            scaleX = logoScale.value
                            scaleY = logoScale.value
                        },
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    text = stringResource(Res.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.alpha(nameAlpha.value),
                )
            }

            Text(
                text = stringResource(Res.string.splash_built_by),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 56.dp)
                    .offset(y = brandOffsetDp.value.dp)
                    .alpha(brandAlpha.value),
            )
        }
    }
}
