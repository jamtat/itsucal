package moe.itsu.common.model.entity.manga

import moe.itsu.common.model.calendar.Calendar
import moe.itsu.common.model.entity.EntityProvidingCalendar
import moe.itsu.common.util.prepareKeyString

data class MangaSeries(
    override val name: String,
    val publisher: String,
    val items: List<Manga> = ArrayList(),
    val otherNames: List<String> = ArrayList(),
    val publisherUrl: String
): EntityProvidingCalendar {
    override val key: String
        get() = "$publisher:${prepareKeyString(name)}"

    override fun toCalendar(): Calendar =
        Calendar(
            events = items.map { it.toCalendarEvent() },
            name = name
        )

    companion object {
        val version = 1
    }
}
