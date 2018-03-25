package moe.itsu.service.resources

import moe.itsu.common.model.calendar.Calendar
import moe.itsu.common.model.entity.manga.Manga
import moe.itsu.common.model.entity.manga.MangaSeries
import moe.itsu.persist.db.RedisEntityDB
import javax.ws.rs.Path

@Path("/mangaseries")
class MangaSeriesResource: EntityProvidingCalendarResource<MangaSeries>(
    MangaSeries::class,
    RedisEntityDB(MangaSeries::class).connect()
) {
    val mangaDB: RedisEntityDB<Manga> = RedisEntityDB(Manga::class).connect()

    fun fetchManga(series: MangaSeries): List<Manga> {
        return series.items.mapNotNull { mangaDB.get(it.toString()) }
    }

    override fun composeItem(item: MangaSeries): Any {
        return object {
            val name = item.name
            val publisher = item.publisher
            val items = fetchManga(item)
            val otherNames = item.otherNames
            val publisherUrl = item.publisherUrl
            val key = item.key
        }
    }

    override fun toCalendar(item: MangaSeries): Calendar {
        return Calendar(
            fetchManga(item).map { it.toCalendarEvent() },
            item.name
        )
    }
}