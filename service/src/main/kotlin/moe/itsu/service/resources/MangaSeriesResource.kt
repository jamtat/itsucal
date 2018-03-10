package moe.itsu.service.resources

import moe.itsu.common.model.entity.manga.MangaSeries
import moe.itsu.persist.db.RedisEntityDB
import javax.ws.rs.Path

@Path("/mangaseries")
class MangaSeriesResource: EntityProvidingCalendarResource<MangaSeries>(
    MangaSeries::class,
    RedisEntityDB(MangaSeries::class).connect()
)