package moe.itsu.common.model.entity.manga

import moe.itsu.common.model.calendar.Calendar
import moe.itsu.common.model.entity.EntityProvidingCalendar

data class MangaSeries(
    val name: String,
    val publisher: String,
    val items: List<Manga> = ArrayList(),
    val otherNames: List<String> = ArrayList(),
    val publisherUrl: String
): EntityProvidingCalendar {

    override fun toCalendar(): Calendar =
        Calendar(
            events = items.map { it.toCalendarEvent() },
            name = name
        )

    override fun hashCode(): Int = (name + publisher).hashCode()
}
