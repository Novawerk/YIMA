package com.haodong.yimalaile.ui.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue

/**
 * Platform-specific locale override for Compose Multiplatform resources.
 * Pass null to follow the system locale.
 */
expect object LocalAppLocale {
    val current: String
        @Composable get

    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}
