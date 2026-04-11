package com.haodong.yimalaile.domain.export

import com.haodong.yimalaile.domain.menstrual.DailyRecord
import com.haodong.yimalaile.domain.menstrual.Intensity
import com.haodong.yimalaile.domain.menstrual.MenstrualRecord
import com.haodong.yimalaile.domain.menstrual.Mood
import com.haodong.yimalaile.domain.menstrual.averagePeriodLength
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.until
import kotlin.time.Clock

/**
 * Platform-independent description of the long image we're going to
 * draw. A renderer (Android Canvas, iOS UIKit) walks this list once
 * to measure heights, a second time to draw.
 */
internal sealed interface ReportLine {
    data class Text(val text: String, val style: ReportTextStyle) : ReportLine
    data class Spacer(val height: Float) : ReportLine
    data object CardBegin : ReportLine
    data object CardEnd : ReportLine
}

internal enum class ReportTextStyle {
    Title,          // 56 bold
    Subtitle,       // 22 muted
    SectionHeading, // 38 bold
    RecordTitle,    // 32 bold
    Body,           // 28
    BulletMuted,    // 22
    SmallMuted,     // 22 muted
}

/** Concrete sizing + weight the renderers use. Shared so both sides match. */
internal data class ReportTextSpec(
    val fontSize: Float,
    val bold: Boolean,
    val muted: Boolean,
    val lineHeightMultiplier: Float,
)

internal fun ReportTextStyle.spec(): ReportTextSpec = when (this) {
    ReportTextStyle.Title -> ReportTextSpec(56f, bold = true, muted = false, lineHeightMultiplier = 1.35f)
    ReportTextStyle.Subtitle -> ReportTextSpec(22f, bold = false, muted = true, lineHeightMultiplier = 1.5f)
    ReportTextStyle.SectionHeading -> ReportTextSpec(38f, bold = true, muted = false, lineHeightMultiplier = 1.4f)
    ReportTextStyle.RecordTitle -> ReportTextSpec(32f, bold = true, muted = false, lineHeightMultiplier = 1.4f)
    ReportTextStyle.Body -> ReportTextSpec(28f, bold = false, muted = false, lineHeightMultiplier = 1.55f)
    ReportTextStyle.BulletMuted -> ReportTextSpec(22f, bold = false, muted = false, lineHeightMultiplier = 1.6f)
    ReportTextStyle.SmallMuted -> ReportTextSpec(22f, bold = false, muted = true, lineHeightMultiplier = 1.55f)
}

/**
 * Shared canvas geometry. Keep these values aligned between Android
 * and iOS renderers so the resulting long images look identical.
 */
internal object ReportLayout {
    const val ImageWidth = 1080
    const val MarginLeft = 64
    const val MarginTop = 96
    const val MarginBottom = 96
    const val CardInset = 24f
    const val CardInnerPadding = 28f
    const val CardCorner = 32f
    const val CardHorizontalBleed = 8f

    const val ColorBackground: Int = 0xFFFAFAFA.toInt()
    const val ColorCardBg: Int = 0xFFF1F3F7.toInt()
    const val ColorText: Int = 0xFF111111.toInt()
    const val ColorMuted: Int = 0xFF666666.toInt()
}

internal object ReportContent {

    fun build(records: List<MenstrualRecord>, strings: ExportStrings): List<ReportLine> = buildList {
        val sorted = records.filter { !it.isDeleted }.sortedBy { it.startDate }

        add(ReportLine.Text(strings.reportTitle, ReportTextStyle.Title))
        add(ReportLine.Spacer(12f))
        add(ReportLine.Text("${strings.generatedOn}: ${formatNow()}", ReportTextStyle.Subtitle))
        add(ReportLine.Spacer(32f))

        // Summary card
        add(ReportLine.CardBegin)
        add(ReportLine.Text(strings.summaryHeader, ReportTextStyle.SectionHeading))
        add(ReportLine.Spacer(8f))
        add(ReportLine.Text("${strings.summaryTotalRecords}: ${sorted.size}", ReportTextStyle.Body))

        val avgCycle = computeAvgCycle(sorted)
        val avgPeriod = averagePeriodLength(sorted)
        add(
            ReportLine.Text(
                "${strings.summaryAverageCycle}: " +
                    (avgCycle?.let { "$it ${strings.unitDays}" } ?: strings.recordNoEnd),
                ReportTextStyle.Body,
            )
        )
        add(
            ReportLine.Text(
                "${strings.summaryAveragePeriod}: " +
                    (avgPeriod?.let { "$it ${strings.unitDays}" } ?: strings.recordNoEnd),
                ReportTextStyle.Body,
            )
        )
        add(ReportLine.CardEnd)
        add(ReportLine.Spacer(32f))

        // Records
        add(ReportLine.Text(strings.recordsHeader, ReportTextStyle.SectionHeading))
        add(ReportLine.Spacer(16f))

        if (sorted.isEmpty()) {
            add(ReportLine.Text(strings.noRecords, ReportTextStyle.BulletMuted))
        } else {
            sorted.forEachIndexed { index, record ->
                addRecordCard(index + 1, record, strings)
                add(ReportLine.Spacer(20f))
            }
        }

        add(ReportLine.Spacer(16f))
        add(ReportLine.Text(strings.disclaimer, ReportTextStyle.SmallMuted))
    }

    fun buildFileName(language: String): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val y = now.year
        val m = now.date.month.number.toString().padStart(2, '0')
        val d = now.date.day.toString().padStart(2, '0')
        val hh = now.hour.toString().padStart(2, '0')
        val mm = now.minute.toString().padStart(2, '0')
        val prefix = if (language == "zh") "月经周期报告" else "CycleReport"
        return "${prefix}_${y}${m}${d}_${hh}${mm}.png"
    }

    // ---------- internals ----------

    private fun MutableList<ReportLine>.addRecordCard(
        indexDisplay: Int,
        record: MenstrualRecord,
        strings: ExportStrings,
    ) {
        add(ReportLine.CardBegin)
        add(ReportLine.Text("${strings.recordIndex} #$indexDisplay", ReportTextStyle.RecordTitle))

        val startStr = record.startDate.toString()
        val endStr = record.endDate?.toString() ?: strings.recordOngoing
        val durationStr = record.endDate?.let {
            val days = record.startDate.until(it, DateTimeUnit.DAY).toInt() + 1
            "$days ${strings.unitDays}"
        } ?: strings.recordNoEnd

        add(ReportLine.Text("$startStr  →  $endStr   ($durationStr)", ReportTextStyle.Body))

        val daily = record.dailyRecords.sortedBy { it.date }
        if (daily.isNotEmpty()) {
            add(ReportLine.Spacer(8f))
            add(ReportLine.Text("${strings.dailyHeader}:", ReportTextStyle.SmallMuted))
            daily.forEach { day ->
                add(ReportLine.Text("• " + formatDaily(day, strings), ReportTextStyle.BulletMuted))
            }
        }
        add(ReportLine.CardEnd)
    }

    private fun formatNow(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val m = now.date.month.number.toString().padStart(2, '0')
        val d = now.date.day.toString().padStart(2, '0')
        val hh = now.hour.toString().padStart(2, '0')
        val mm = now.minute.toString().padStart(2, '0')
        return "${now.year}-$m-$d $hh:$mm"
    }

    private fun formatDaily(day: DailyRecord, strings: ExportStrings): String {
        val parts = mutableListOf<String>()
        parts.add(day.date.toString())
        day.intensity?.let { parts.add("${strings.dailyIntensity}:${intensityLabel(it, strings)}") }
        day.mood?.let { parts.add("${strings.dailyMood}:${moodLabel(it, strings)}") }
        if (day.symptoms.isNotEmpty()) {
            parts.add("${strings.dailySymptoms}:${day.symptoms.joinToString(",")}")
        }
        if (!day.notes.isNullOrBlank()) {
            parts.add("${strings.dailyNotes}:${day.notes}")
        }
        return parts.joinToString("  ")
    }

    private fun intensityLabel(intensity: Intensity, s: ExportStrings) = when (intensity) {
        Intensity.LIGHT -> s.intensityLight
        Intensity.MEDIUM -> s.intensityMedium
        Intensity.HEAVY -> s.intensityHeavy
    }

    private fun moodLabel(mood: Mood, s: ExportStrings) = when (mood) {
        Mood.HAPPY -> s.moodHappy
        Mood.NEUTRAL -> s.moodNeutral
        Mood.SAD -> s.moodSad
        Mood.VERY_SAD -> s.moodVerySad
    }

    private fun computeAvgCycle(records: List<MenstrualRecord>): Int? {
        val lens = mutableListOf<Int>()
        for (i in 0 until records.size - 1) {
            val len = records[i].startDate.until(records[i + 1].startDate, DateTimeUnit.DAY).toInt()
            if (len in 14..90) lens.add(len)
        }
        return if (lens.isEmpty()) null else lens.sum() / lens.size
    }
}
