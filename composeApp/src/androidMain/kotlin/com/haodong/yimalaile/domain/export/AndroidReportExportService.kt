package com.haodong.yimalaile.domain.export

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Android implementation of [ReportExportService].
 *
 * Renders the shared [ReportContent] block list into a long PNG via
 * [Bitmap] + [Canvas], writes it to the app's cache directory, then
 * hands a `content://` URI (via `FileProvider`) to the system share
 * sheet so the user can send it to WeChat / Photos / wherever in one
 * tap.
 */
class AndroidReportExportService(private val context: Context) : ReportExportService {

    override suspend fun exportCycleReport(
        records: List<MenstrualRecord>,
        language: String,
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            val strings = ExportStrings.forLanguage(language)
            val lines = ReportContent.build(records, strings)

            val plan = measure(lines)
            val bitmap = renderBitmap(plan)
            val file = writeToCache(bitmap, ReportContent.buildFileName(language))
            bitmap.recycle()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
            launchShareSheet(uri)

            ExportResult.Success(displayLocation = file.name)
        } catch (t: Throwable) {
            ExportResult.Failure(message = t.message ?: "Unknown error")
        }
    }

    // ---------- Measure pass ----------

    private fun measure(lines: List<ReportLine>): RenderPlan {
        val commands = mutableListOf<DrawCommand>()
        var cursorY: Float = ReportLayout.MarginTop.toFloat()
        var cardStart: Float? = null
        val measurePaint = Paint().apply {
            isAntiAlias = true
            typeface = Typeface.DEFAULT
        }

        for (line in lines) {
            when (line) {
                is ReportLine.Text -> {
                    val spec = line.style.spec()
                    val inset = if (cardStart != null) ReportLayout.CardInset else 0f
                    val maxWidth =
                        ReportLayout.ImageWidth - ReportLayout.MarginLeft * 2 - inset
                    measurePaint.textSize = spec.fontSize
                    measurePaint.isFakeBoldText = spec.bold
                    val wrapped = wrap(line.text, maxWidth, measurePaint)
                    val lineHeight = spec.fontSize * spec.lineHeightMultiplier
                    val color =
                        if (spec.muted) ReportLayout.ColorMuted else ReportLayout.ColorText
                    for (segment in wrapped) {
                        val topY = cursorY
                        commands.add(
                            DrawCommand.Text(
                                text = segment,
                                size = spec.fontSize,
                                bold = spec.bold,
                                color = color,
                                topY = topY,
                                inset = inset,
                            )
                        )
                        cursorY += lineHeight
                    }
                }
                is ReportLine.Spacer -> cursorY += line.height
                ReportLine.CardBegin -> {
                    cardStart = cursorY
                    cursorY += ReportLayout.CardInnerPadding
                }
                ReportLine.CardEnd -> {
                    cursorY += ReportLayout.CardInnerPadding
                    commands.add(
                        DrawCommand.Rect(
                            top = cardStart!!,
                            bottom = cursorY,
                            color = ReportLayout.ColorCardBg,
                            cornerRadius = ReportLayout.CardCorner,
                        )
                    )
                    cardStart = null
                }
            }
        }

        val totalHeight = (cursorY + ReportLayout.MarginBottom).toInt()
        return RenderPlan(commands, totalHeight)
    }

    private fun wrap(text: String, maxWidth: Float, paint: Paint): List<String> {
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
                if (paint.measureText(candidate) > maxWidth && current.isNotEmpty()) {
                    lines.add(current.toString())
                    current.clear()
                }
                current.append(ch)
            }
            if (current.isNotEmpty()) lines.add(current.toString())
        }
        return lines
    }

    // ---------- Render pass ----------

    private fun renderBitmap(plan: RenderPlan): Bitmap {
        val bitmap = Bitmap.createBitmap(
            ReportLayout.ImageWidth,
            plan.totalHeight.coerceAtLeast(MIN_HEIGHT),
            Bitmap.Config.ARGB_8888,
        )
        val canvas = Canvas(bitmap)
        canvas.drawColor(ReportLayout.ColorBackground)

        val textPaint = Paint().apply {
            isAntiAlias = true
            typeface = Typeface.DEFAULT
        }
        val fillPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        // Pass 1: card backgrounds behind text.
        for (cmd in plan.commands) {
            if (cmd is DrawCommand.Rect) {
                fillPaint.color = cmd.color
                val left = ReportLayout.MarginLeft - ReportLayout.CardHorizontalBleed
                val right =
                    (ReportLayout.ImageWidth - ReportLayout.MarginLeft) + ReportLayout.CardHorizontalBleed
                canvas.drawRoundRect(
                    left, cmd.top, right, cmd.bottom,
                    cmd.cornerRadius, cmd.cornerRadius,
                    fillPaint,
                )
            }
        }
        // Pass 2: text on top.
        for (cmd in plan.commands) {
            if (cmd is DrawCommand.Text) {
                textPaint.textSize = cmd.size
                textPaint.color = cmd.color
                textPaint.isFakeBoldText = cmd.bold
                val baseline = cmd.topY + cmd.size
                canvas.drawText(
                    cmd.text,
                    (ReportLayout.MarginLeft + cmd.inset),
                    baseline,
                    textPaint,
                )
            }
        }
        return bitmap
    }

    // ---------- File saving + sharing ----------

    private fun writeToCache(bitmap: Bitmap, fileName: String): File {
        val dir = File(context.cacheDir, "exports")
        if (!dir.exists()) dir.mkdirs()
        // Clean up older exports so we don't accumulate files in cache.
        dir.listFiles()?.forEach { old ->
            if (old.isFile) old.delete()
        }
        val file = File(dir, fileName)
        FileOutputStream(file).use { os ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
            os.flush()
        }
        return file
    }

    private fun launchShareSheet(uri: android.net.Uri) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            // ClipData + ACTION_SEND ensures every app in the chooser
            // actually receives read permission to the content URI.
            clipData = ClipData.newRawUri(null, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(send, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(chooser)
    }

    // ---------- Models ----------

    private data class RenderPlan(
        val commands: List<DrawCommand>,
        val totalHeight: Int,
    )

    private sealed class DrawCommand {
        data class Text(
            val text: String,
            val size: Float,
            val color: Int,
            val bold: Boolean,
            val topY: Float,
            val inset: Float,
        ) : DrawCommand()

        data class Rect(
            val top: Float,
            val bottom: Float,
            val color: Int,
            val cornerRadius: Float,
        ) : DrawCommand()
    }

    companion object {
        private const val MIN_HEIGHT = 800
    }
}
