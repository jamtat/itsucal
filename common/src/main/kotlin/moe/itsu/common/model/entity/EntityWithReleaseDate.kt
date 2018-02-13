package moe.itsu.common.model.entity

import moe.itsu.common.model.calendar.CalendarEvent
import java.time.LocalDate

interface EntityWithReleaseDate {
    val releaseDate: LocalDate

    fun toCalendarEvent(): CalendarEvent
}