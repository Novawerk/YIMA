package com.haodong.yimalaile.domain.menstrual

import kotlinx.datetime.LocalDate

interface DailyNoteRepository {
    suspend fun getNote(date: LocalDate): DailyNote?
    suspend fun getAllNotes(): List<DailyNote>
    suspend fun saveNote(note: DailyNote)
    suspend fun deleteNote(date: LocalDate)
}
