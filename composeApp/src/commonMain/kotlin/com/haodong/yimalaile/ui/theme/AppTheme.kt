package com.haodong.yimalaile.ui.theme

import com.haodong.yimalaile.domain.settings.AppDarkMode
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import yimalaile.composeapp.generated.resources.Res
import yimalaile.composeapp.generated.resources.comfortaa_regular

@Composable
private fun appTypography(): Typography {
    val comfortaa = FontFamily(
        Font(Res.font.comfortaa_regular, FontWeight.Light),
        Font(Res.font.comfortaa_regular, FontWeight.Normal),
        Font(Res.font.comfortaa_regular, FontWeight.Medium),
        Font(Res.font.comfortaa_regular, FontWeight.SemiBold),
        Font(Res.font.comfortaa_regular, FontWeight.Bold),
    )
    val d = Typography()
    return Typography(
        displayLarge = d.displayLarge.copy(fontFamily = comfortaa),
        displayMedium = d.displayMedium.copy(fontFamily = comfortaa),
        displaySmall = d.displaySmall.copy(fontFamily = comfortaa),
        headlineLarge = d.headlineLarge.copy(
            fontFamily = comfortaa,
            fontWeight = FontWeight.Bold,
            fontSize = 56.sp,
            letterSpacing = (-2).sp
        ),
        headlineMedium = d.headlineMedium.copy(
            fontFamily = comfortaa,
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp,
            letterSpacing = (-1).sp
        ),
        headlineSmall = d.headlineSmall.copy(fontFamily = comfortaa),
        titleLarge = d.titleLarge.copy(fontFamily = comfortaa, fontWeight = FontWeight.SemiBold),
        titleMedium = d.titleMedium.copy(fontFamily = comfortaa, fontWeight = FontWeight.SemiBold),
        titleSmall = d.titleSmall.copy(fontFamily = comfortaa),
        bodyLarge = d.bodyLarge.copy(fontFamily = comfortaa),
        bodyMedium = d.bodyMedium.copy(fontFamily = comfortaa),
        bodySmall = d.bodySmall.copy(fontFamily = comfortaa),
        labelLarge = d.labelLarge.copy(fontFamily = comfortaa),
        labelMedium = d.labelMedium.copy(fontFamily = comfortaa, fontWeight = FontWeight.SemiBold),
        labelSmall = d.labelSmall.copy(fontFamily = comfortaa),
        // Emphasized variants — bolder weight for emphasis within each level
        displayLargeEmphasized = d.displayLarge.copy(fontFamily = comfortaa, fontWeight = FontWeight.ExtraBold),
        displayMediumEmphasized = d.displayMedium.copy(fontFamily = comfortaa, fontWeight = FontWeight.Bold),
        displaySmallEmphasized = d.displaySmall.copy(fontFamily = comfortaa, fontWeight = FontWeight.Bold),
        headlineLargeEmphasized = d.headlineLarge.copy(
            fontFamily = comfortaa,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 56.sp,
            letterSpacing = (-2).sp
        ),
        headlineMediumEmphasized = d.headlineMedium.copy(
            fontFamily = comfortaa,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 36.sp,
            letterSpacing = (-1).sp
        ),
        headlineSmallEmphasized = d.headlineSmall.copy(fontFamily = comfortaa, fontWeight = FontWeight.Bold),
        titleLargeEmphasized = d.titleLarge.copy(fontFamily = comfortaa, fontWeight = FontWeight.Bold),
        titleMediumEmphasized = d.titleMedium.copy(fontFamily = comfortaa, fontWeight = FontWeight.Bold),
        titleSmallEmphasized = d.titleSmall.copy(fontFamily = comfortaa, fontWeight = FontWeight.SemiBold),
        bodyLargeEmphasized = d.bodyLarge.copy(fontFamily = comfortaa, fontWeight = FontWeight.SemiBold),
        bodyMediumEmphasized = d.bodyMedium.copy(fontFamily = comfortaa, fontWeight = FontWeight.SemiBold),
        bodySmallEmphasized = d.bodySmall.copy(fontFamily = comfortaa, fontWeight = FontWeight.SemiBold),
        labelLargeEmphasized = d.labelLarge.copy(fontFamily = comfortaa, fontWeight = FontWeight.Bold),
        labelMediumEmphasized = d.labelMedium.copy(fontFamily = comfortaa, fontWeight = FontWeight.Bold),
        labelSmallEmphasized = d.labelSmall.copy(fontFamily = comfortaa, fontWeight = FontWeight.SemiBold),
    )
}

// ============================================================
// Expressive MaterialShapes — use via MaterialTheme.expressiveShapes
// ============================================================

/**
 * Holds [MaterialShapes] converted to [Shape] for use with Modifier.clip().
 * These are organic shapes that can't go in the standard [Shapes] (which only takes CornerBasedShape).
 *
 * Usage: `Modifier.clip(MaterialTheme.expressiveShapes.cookie4)`
 */
data class ExpressiveShapes(
    // Row 1: Basic
    val circle: Shape = RoundedCornerShape(50),
    val square: Shape = RoundedCornerShape(0.dp),
    val slanted: Shape = RoundedCornerShape(12.dp),
    val arch: Shape = RoundedCornerShape(12.dp),
    val semicircle: Shape = RoundedCornerShape(12.dp),
    val oval: Shape = RoundedCornerShape(12.dp),
    val pill: Shape = RoundedCornerShape(50),
    // Row 2: Geometric
    val triangle: Shape = RoundedCornerShape(12.dp),
    val arrow: Shape = RoundedCornerShape(12.dp),
    val fan: Shape = RoundedCornerShape(12.dp),
    val diamond: Shape = RoundedCornerShape(12.dp),
    val clamShell: Shape = RoundedCornerShape(12.dp),
    val pentagon: Shape = RoundedCornerShape(12.dp),
    val gem: Shape = RoundedCornerShape(12.dp),
    // Row 3: Organic
    val sunny: Shape = RoundedCornerShape(12.dp),
    val verySunny: Shape = RoundedCornerShape(12.dp),
    val cookie4: Shape = RoundedCornerShape(12.dp),
    val cookie6: Shape = RoundedCornerShape(12.dp),
    val cookie7: Shape = RoundedCornerShape(12.dp),
    val cookie9: Shape = RoundedCornerShape(12.dp),
    val cookie12: Shape = RoundedCornerShape(12.dp),
    // Row 4: Playful
    val clover4: Shape = RoundedCornerShape(12.dp),
    val clover8: Shape = RoundedCornerShape(12.dp),
    val burst: Shape = RoundedCornerShape(12.dp),
    val softBurst: Shape = RoundedCornerShape(12.dp),
    val boom: Shape = RoundedCornerShape(12.dp),
    val softBoom: Shape = RoundedCornerShape(12.dp),
    val flower: Shape = RoundedCornerShape(12.dp),
    // Row 5: Decorative
    val puffy: Shape = RoundedCornerShape(12.dp),
    val puffyDiamond: Shape = RoundedCornerShape(12.dp),
    val ghostish: Shape = RoundedCornerShape(12.dp),
    val pixelCircle: Shape = RoundedCornerShape(12.dp),
    val pixelTriangle: Shape = RoundedCornerShape(12.dp),
    val bun: Shape = RoundedCornerShape(12.dp),
    val heart: Shape = RoundedCornerShape(12.dp),
)

val LocalExpressiveShapes = staticCompositionLocalOf { ExpressiveShapes() }

/** Access expressive shapes via MaterialTheme. */
val MaterialTheme.expressiveShapes: ExpressiveShapes
    @Composable get() = LocalExpressiveShapes.current

/** Build [ExpressiveShapes] with actual MaterialShapes. Must be called in @Composable context. */
@Composable
private fun expressiveShapes() = ExpressiveShapes(
    // Basic
    circle = MaterialShapes.Circle.toShape(),
    square = MaterialShapes.Square.toShape(),
    slanted = MaterialShapes.Slanted.toShape(),
    arch = MaterialShapes.Arch.toShape(),
    semicircle = MaterialShapes.SemiCircle.toShape(),
    oval = MaterialShapes.Oval.toShape(),
    pill = MaterialShapes.Pill.toShape(),
    // Geometric
    triangle = MaterialShapes.Triangle.toShape(),
    arrow = MaterialShapes.Arrow.toShape(),
    fan = MaterialShapes.Fan.toShape(),
    diamond = MaterialShapes.Diamond.toShape(),
    clamShell = MaterialShapes.ClamShell.toShape(),
    pentagon = MaterialShapes.Pentagon.toShape(),
    gem = MaterialShapes.Gem.toShape(),
    // Organic
    sunny = MaterialShapes.Sunny.toShape(),
    verySunny = MaterialShapes.VerySunny.toShape(),
    cookie4 = MaterialShapes.Cookie4Sided.toShape(),
    cookie6 = MaterialShapes.Cookie6Sided.toShape(),
    cookie7 = MaterialShapes.Cookie7Sided.toShape(),
    cookie9 = MaterialShapes.Cookie9Sided.toShape(),
    cookie12 = MaterialShapes.Cookie12Sided.toShape(),
    // Playful
    clover4 = MaterialShapes.Clover4Leaf.toShape(),
    clover8 = MaterialShapes.Clover8Leaf.toShape(),
    burst = MaterialShapes.Burst.toShape(),
    softBurst = MaterialShapes.SoftBurst.toShape(),
    boom = MaterialShapes.Boom.toShape(),
    softBoom = MaterialShapes.SoftBoom.toShape(),
    flower = MaterialShapes.Flower.toShape(),
    // Decorative
    puffy = MaterialShapes.Puffy.toShape(),
    puffyDiamond = MaterialShapes.PuffyDiamond.toShape(),
    ghostish = MaterialShapes.Ghostish.toShape(),
    pixelCircle = MaterialShapes.PixelCircle.toShape(),
    pixelTriangle = MaterialShapes.PixelTriangle.toShape(),
    bun = MaterialShapes.Bun.toShape(),
    heart = MaterialShapes.Heart.toShape(),
)

// ============================================================
// Theme composable
// ============================================================

@Composable
fun AppTheme(
    darkMode: AppDarkMode = AppDarkMode.SYSTEM,
    content: @Composable () -> Unit
) {


    val isDarkTheme = when (darkMode) {
        AppDarkMode.DARK -> true
        AppDarkMode.LIGHT -> false
        AppDarkMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme =
        when {
            isDarkTheme -> darkColorScheme()
            else -> expressiveLightColorScheme()
        }


    CompositionLocalProvider(LocalExpressiveShapes provides expressiveShapes()) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            typography = appTypography(),
            shapes = Shapes(
                extraSmall = RoundedCornerShape(8.dp),
                small = RoundedCornerShape(12.dp),
                medium = RoundedCornerShape(16.dp),
                large = RoundedCornerShape(24.dp),
                extraLarge = RoundedCornerShape(32.dp),
                largeIncreased = RoundedCornerShape(28.dp),
                extraLargeIncreased = RoundedCornerShape(36.dp),
                extraExtraLarge = RoundedCornerShape(40.dp),
            ),
            motionScheme = MotionScheme.expressive(),
            content = content,
        )
    }
}
