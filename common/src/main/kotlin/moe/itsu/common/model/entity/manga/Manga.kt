package moe.itsu.common.model.entity.manga

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import moe.itsu.common.model.calendar.CalendarEvent
import moe.itsu.common.model.entity.EntityWithReleaseDate
import moe.itsu.common.util.LocalDateDeserialiser
import moe.itsu.common.util.LocalDateSerialiser
import java.time.LocalDate

data class Manga (
    override val name: String,
    @get:JsonSerialize(using = LocalDateSerialiser::class)
    @param:JsonDeserialize(using = LocalDateDeserialiser::class)
    override val releaseDate: LocalDate,
    val isbn13: ISBN13,
    val format: MangaFormat = MangaFormat.PRINT,
    val pageCount: Int = 0,
    val authors: List<String> = ArrayList(),
    val publisherUrl: String,
    val blurb: String? = null
) : EntityWithReleaseDate {
    override val key: String
        get() = isbn13.toString()

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

    companion object {
        val version = 1
    }
}
