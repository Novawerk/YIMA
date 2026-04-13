package com.haodong.yimalaile.domain.export

import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGFloat
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToURL
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIBezierPath
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIGraphicsImageRendererFormat
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation
import platform.UIKit.UIRectFill
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.drawAtPoint

/**
 * iOS implementation of [ReportExportService].
 *
 * Walks the shared [ReportContent] block list to build a list of
 * draw commands (measure pass), then paints them into a
 * [UIGraphicsImageRenderer] context. The resulting PNG is written to
 * the app's Caches directory and handed to a
 * [UIActivityViewController] so the user can share it or save it to
 * Photos from the system share sheet.
 *
 * Text width is estimated (half-width ≈ 0.55 × size, full-width ≈ 1.0 × size)
 * instead of routing through UIKit font metrics — the long image is
 * a static report, not a typography-critical layout, and this keeps
 * the renderer free of CValue gymnastics at the Kotlin/Native boundary.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosReportExportService : ReportExportService {

    override suspend fun exportCycleReport(
        records: List<MenstrualRecord>,
        language: String,
    ): ExportResult {
        return try {
            val strings = ExportStrings.forLanguage(language)
            val lines = ReportContent.build(records, strings)

            val plan = measure(lines)
            val image = renderImage(plan)

            val pngData = UIImagePNGRepresentation(image)
                ?: return ExportResult.Failure("Failed to encode PNG")

            val fileName = ReportContent.buildFileName(language)
            val fileUrl = writeToCaches(pngData, fileName)
                ?: return ExportResult.Failure("Failed to write temp file")

            presentActivityViewController(fileUrl)

            ExportResult.Success(displayLocation = fileName)
        } catch (t: Throwable) {
            ExportResult.Failure(message = t.message ?: "Unknown error")
        }
    }

    // ---------- Measure pass ----------

    private data class TextCmd(
        val text: String,
        val size: CGFloat,
        val bold: Boolean,
        val colorArgb: Int,
        val topY: CGFloat,
        val inset: CGFloat,
    )

    private data class RectCmd(
        val top: CGFloat,
        val bottom: CGFloat,
        val colorArgb: Int,
        val cornerRadius: CGFloat,
    )

    private data class RenderPlan(
        val texts: List<TextCmd>,
        val rects: List<RectCmd>,
        val totalHeight: CGFloat,
    )

    private fun measure(lines: List<ReportLine>): RenderPlan {
        val texts = mutableListOf<TextCmd>()
        val rects = mutableListOf<RectCmd>()
        var cursorY: CGFloat = ReportLayout.MarginTop.toDouble()
        var cardStart: CGFloat? = null

        for (line in lines) {
            when (line) {
                is ReportLine.Text -> {
                    val spec = line.style.spec()
                    val inset: CGFloat =
                        if (cardStart != null) ReportLayout.CardInset.toDouble() else 0.0
                    val maxWidth: CGFloat =
                        (ReportLayout.ImageWidth - ReportLayout.MarginLeft * 2).toDouble() - inset
                    val lineHeight: CGFloat =
                        (spec.fontSize * spec.lineHeightMultiplier).toDouble()
                    val color =
                        if (spec.muted) ReportLayout.ColorMuted else ReportLayout.ColorText
                    for (segment in wrap(line.text, maxWidth, spec.fontSize.toDouble())) {
                        texts.add(
                            TextCmd(
                                text = segment,
                                size = spec.fontSize.toDouble(),
                                bold = spec.bold,
                                colorArgb = color,
                                topY = cursorY,
                                inset = inset,
                            )
                        )
                        cursorY += lineHeight
                    }
                }
                is ReportLine.Spacer -> cursorY += line.height.toDouble()
                ReportLine.CardBegin -> {
                    cardStart = cursorY
                    cursorY += ReportLayout.CardInnerPadding.toDouble()
                }
                ReportLine.CardEnd -> {
                    cursorY += ReportLayout.CardInnerPadding.toDouble()
                    rects.add(
                        RectCmd(
                            top = cardStart!!,
                            bottom = cursorY,
                            colorArgb = ReportLayout.ColorCardBg,
                            cornerRadius = ReportLayout.CardCorner.toDouble(),
                        )
                    )
                    cardStart = null
                }
            }
        }

        val totalHeight = cursorY + ReportLayout.MarginBottom.toDouble()
        return RenderPlan(texts, rects, totalHeight)
    }

    /**
     * Character-wise wrap using an approximate width per glyph —
     * ASCII counts as half-width, everything else (incl. CJK) as
     * full-width. Matches the intent of the Android implementation
     * without having to call into UIKit text metrics.
     */
    private fun wrap(text: String, maxWidth: CGFloat, fontSize: CGFloat): List<String> {
        if (text.isEmpty()) return listOf("")
        val lines = mutableListOf<String>()
        for (rawLine in text.split('\n')) {
            if (rawLine.isEmpty()) {
                lines.add("")
                continue
            }
            val current = StringBuilder()
            for (ch in rawLine) {
                val candidate = current.toString() + ch
                if (measureWidth(candidate, fontSize) > maxWidth && current.isNotEmpty()) {
                    lines.add(current.toString())
                    current.clear()
                }
                current.append(ch)
            }
            if (current.isNotEmpty()) lines.add(current.toString())
        }
        return lines
    }

    private fun measureWidth(text: String, fontSize: CGFloat): CGFloat {
        var width = 0.0
        for (ch in text) {
            width += if (ch.code < 0x80) fontSize * 0.55 else fontSize * 1.0
        }
        return width
    }

    // ---------- Render pass ----------

    private fun renderImage(plan: RenderPlan): UIImage {
        val width: CGFloat = ReportLayout.ImageWidth.toDouble()
        val height: CGFloat = maxOf(plan.totalHeight, 800.0)
        val sizeValue = CGSizeMake(width, height)

        val format = UIGraphicsImageRendererFormat.defaultFormat()
        format.setScale(1.0) // 1 px per point — predictable output dimensions
        val renderer = UIGraphicsImageRenderer(size = sizeValue, format = format)

        return renderer.imageWithActions { _ ->
            // Background fill
            argbToUiColor(ReportLayout.ColorBackground).setFill()
            UIRectFill(CGRectMake(0.0, 0.0, width, height))

            // Card backgrounds first
            for (rect in plan.rects) {
                val left =
                    (ReportLayout.MarginLeft - ReportLayout.CardHorizontalBleed).toDouble()
                val right =
                    (ReportLayout.ImageWidth - ReportLayout.MarginLeft + ReportLayout.CardHorizontalBleed).toDouble()
                val cgRect = CGRectMake(
                    left,
                    rect.top,
                    right - left,
                    rect.bottom - rect.top,
                )
                val path = UIBezierPath.bezierPathWithRoundedRect(
                    rect = cgRect,
                    cornerRadius = rect.cornerRadius,
                )
                argbToUiColor(rect.colorArgb).setFill()
                path.fill()
            }

            // Text on top
            for (cmd in plan.texts) {
                val font = uiFontFor(cmd.size, cmd.bold)
                val color = argbToUiColor(cmd.colorArgb)
                val attrs = mapOf<Any?, Any>(
                    NSFontAttributeName to font,
                    NSForegroundColorAttributeName to color,
                )
                val ns = NSString.create(string = cmd.text)
                val x = ReportLayout.MarginLeft + cmd.inset
                ns.drawAtPoint(
                    point = CGPointMake(x, cmd.topY),
                    withAttributes = attrs as Map<Any?, *>,
                )
            }
        }
    }

    private fun uiFontFor(size: CGFloat, bold: Boolean): UIFont =
        if (bold) UIFont.boldSystemFontOfSize(size) else UIFont.systemFontOfSize(size)

    private fun argbToUiColor(argb: Int): UIColor {
        val a = ((argb ushr 24) and 0xFF) / 255.0
        val r = ((argb ushr 16) and 0xFF) / 255.0
        val g = ((argb ushr 8) and 0xFF) / 255.0
        val b = (argb and 0xFF) / 255.0
        return UIColor(red = r, green = g, blue = b, alpha = a)
    }

    // ---------- File saving + sharing ----------

    private fun writeToCaches(data: NSData, fileName: String): NSURL? {
        val dirs = NSSearchPathForDirectoriesInDomains(
            directory = NSCachesDirectory,
            domainMask = NSUserDomainMask,
            expandTilde = true,
        )
        val base = (dirs.firstOrNull() as? String) ?: return null
        val exportsDir = "$base/exports"

        val fm = NSFileManager.defaultManager
        fm.createDirectoryAtPath(
            path = exportsDir,
            withIntermediateDirectories = true,
            attributes = null,
            error = null,
        )
        // Best-effort cleanup of older exports so we don't leak disk.
        val existing = fm.contentsOfDirectoryAtPath(exportsDir, error = null) ?: emptyList<Any?>()
        for (entry in existing) {
            val name = entry as? String ?: continue
            fm.removeItemAtPath("$exportsDir/$name", error = null)
        }

        val path = "$exportsDir/$fileName"
        val url = NSURL.fileURLWithPath(path)
        val ok = data.writeToURL(url, atomically = true)
        return if (ok) url else null
    }

    private fun presentActivityViewController(imageUrl: NSURL) {
        val items = listOf<Any>(imageUrl)
        val activityVC = UIActivityViewController(
            activityItems = items,
            applicationActivities = null,
        )

        val rootVC = findPresentingViewController() ?: return
        rootVC.presentViewController(activityVC, animated = true, completion = null)
    }

    private fun findPresentingViewController(): UIViewController? {
        val app = UIApplication.sharedApplication
        val keyWindow: UIWindow? = app.keyWindow
            ?: (app.windows.firstOrNull() as? UIWindow)
        var top: UIViewController? = keyWindow?.rootViewController
        while (top?.presentedViewController != null) {
            top = top.presentedViewController
        }
        return top
    }
}
