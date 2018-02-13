package moe.itsu.common.model.entity.manga

import moe.itsu.common.model.calendar.CalendarEvent
import moe.itsu.common.model.entity.EntityWithReleaseDate
import java.time.LocalDate

data class Manga (
    val name: String,
    override val releaseDate: LocalDate,
    val isbn13: ISBN13,
    val format: MangaFormat = MangaFormat.PRINT,
    val pageCount: Int = 0,
    val authors: List<String> = ArrayList(),
    val publisherUrl: String,
    val blurb: String? = null
) : EntityWithReleaseDate {

    override fun toCalendarEvent(): CalendarEvent =
        CalendarEvent(
            name = name,
            date = releaseDate
        )

    override fun equals(other: Any?): Boolean = when(other) {
        is Manga -> isbn13 == other.isbn13
        else -> false
    }

    override fun hashCode(): Int = isbn13.toString().hashCode()
}
