package moe.itsu.common.model.entity

import moe.itsu.common.model.calendar.CalendarEvent
import java.time.LocalDate


interface EntityWithReleaseDate: Entity {
    // @get:JsonSerialize(using = LocalDateSerialiser::class)
    // @param:JsonDeserialize(using = LocalDateDeserialiser::class)
    val releaseDate: LocalDate

    fun toCalendarEvent(): CalendarEvent
}