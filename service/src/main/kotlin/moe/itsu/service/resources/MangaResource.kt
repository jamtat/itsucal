package moe.itsu.service.resources

import moe.itsu.common.model.calendar.Calendar
import moe.itsu.common.model.entity.manga.Manga
import moe.itsu.persist.db.RedisEntityDB
import moe.itsu.scrape.vendor.bookdepository.BookDepositoryPurchaseInformationProvider
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/manga")
class MangaResource: EntityProvidingCalendarResource<Manga>(
    Manga::class,
    RedisEntityDB(Manga::class).connect()
) {
    override fun toCalendar(item: Manga): Calendar =
        Calendar(
            listOf(item.toCalendarEvent()),
            item.name
        )

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/purchase/{key}.json")
    fun getPurchaseInformation(
        @PathParam("key") key: String
    ): Response {
        val item = db.get(key)
        if (item == null)
            return Response.status(Response.Status.NOT_FOUND).build()

        val purchaseInfo = BookDepositoryPurchaseInformationProvider().get(item)
        return Response.ok(purchaseInfo).build()
    }

}