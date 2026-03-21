package com.haodong.yimalaile.ui.theme

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.graphics.Color

object AppColors {
    val Primary = Color(0xFF8B5E5E)
    val Accent = Color(0xFFF2D5C4)
    val BackgroundLight = Color(0xFFFDF8F5)
    val TextSecondary = Primary.copy(alpha = 0.6f)
    val CardBg1 = Color(0xFFF2D5C4).copy(alpha = 0.4f)
    val CardBg2 = Color(0xFF8B5E5E).copy(alpha = 0.1f)
    val MutedHeart = Color(0xFFC4A4A4)
    val Success = Color(0xFF90A17D)
    val Warning = Color(0xFFD4A373)
}

object AppShapes {
    val LeftBlob = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        moveTo(w * 0.4f, 0f)
        cubicTo(w * 0.8f, 0f, w, h * 0.3f, w, h * 0.5f)
        cubicTo(w, h * 0.8f, w * 0.7f, h, w * 0.4f, h)
        cubicTo(w * 0.1f, h, 0f, h * 0.7f, 0f, h * 0.4f)
        cubicTo(0f, h * 0.2f, w * 0.2f, 0f, w * 0.4f, 0f)
        close()
    }

    val RightBlob = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        moveTo(w * 0.5f, 0f)
        cubicTo(w * 0.9f, 0f, w, h * 0.4f, w, h * 0.6f)
        cubicTo(w, h * 0.9f, w * 0.6f, h, w * 0.4f, h)
        cubicTo(w * 0.1f, h, 0f, h * 0.7f, 0f, h * 0.4f)
        cubicTo(0f, h * 0.1f, w * 0.2f, 0f, w * 0.5f, 0f)
        close()
    }

    val HeroBlob = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        moveTo(w * 0.42f, 0f)
        cubicTo(w * 0.7f, 0f, w, h * 0.2f, w, h * 0.45f)
        cubicTo(w, h * 0.75f, w * 0.7f, h, w * 0.42f, h)
        cubicTo(w * 0.15f, h, 0f, h * 0.75f, 0f, h * 0.45f)
        cubicTo(0f, h * 0.2f, w * 0.2f, 0f, w * 0.42f, 0f)
        close()
    }

    val Blob1 = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        moveTo(w * 0.4f, 0f)
        cubicTo(w * 0.7f, 0f, w, h * 0.3f, w, h * 0.5f)
        cubicTo(w, h * 0.8f, w * 0.6f, h, w * 0.4f, h)
        cubicTo(w * 0.1f, h, 0f, h * 0.7f, 0f, h * 0.4f)
        cubicTo(0f, h * 0.1f, w * 0.2f, 0f, w * 0.4f, 0f)
        close()
    }

    val Blob2 = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        moveTo(w * 0.5f, 0f)
        cubicTo(w * 0.9f, 0f, w, h * 0.4f, w, h * 0.6f)
        cubicTo(w, h * 0.9f, w * 0.7f, h, w * 0.5f, h)
        cubicTo(w * 0.2f, h, 0f, h * 0.8f, 0f, h * 0.5f)
        cubicTo(0f, h * 0.2f, w * 0.1f, 0f, w * 0.5f, 0f)
        close()
    }

    val Blob3 = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        moveTo(w * 0.6f, 0f)
        cubicTo(w * 0.8f, 0f, w, h * 0.2f, w, h * 0.4f)
        cubicTo(w, h * 0.7f, w * 0.8f, h, w * 0.6f, h)
        cubicTo(w * 0.3f, h, 0f, h * 0.8f, 0f, h * 0.6f)
        cubicTo(0f, h * 0.3f, w * 0.2f, 0f, w * 0.6f, 0f)
        close()
    }

    val DateBlob = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        moveTo(w * 0.5f, 0f)
        cubicTo(w * 0.8f, 0f, w, h * 0.2f, w, h * 0.5f)
        cubicTo(w, h * 0.8f, w * 0.7f, h, w * 0.4f, h)
        cubicTo(w * 0.1f, h, 0f, h * 0.7f, 0f, h * 0.4f)
        cubicTo(0f, h * 0.2f, w * 0.2f, 0f, w * 0.5f, 0f)
        close()
    }

    val SymptomChipShape = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        moveTo(w * 0.2f, 0f)
        cubicTo(w * 0.8f, 0f, w, h * 0.2f, w, h * 0.5f)
        cubicTo(w, h * 0.8f, w * 0.7f, h, w * 0.4f, h)
        cubicTo(w * 0.1f, h, 0f, h * 0.7f, 0f, h * 0.4f)
        cubicTo(0f, h * 0.1f, w * 0.1f, 0f, w * 0.2f, 0f)
        close()
    }
}
