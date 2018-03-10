package moe.itsu.service.resources

import moe.itsu.common.model.entity.EntityProvidingCalendar
import moe.itsu.persist.api.EntityDB
import moe.itsu.persist.db.KeyValueInMemoryEntityDB
import moe.itsu.service.search.SearchService
import moe.itsu.service.search.SimpleEntitySearchService
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import kotlin.reflect.KClass


abstract class EntityProvidingCalendarResource<T: EntityProvidingCalendar>(
    private val entityType: Class<T>,
    private val db: EntityDB<T> = KeyValueInMemoryEntityDB(),
    private val searchService: SearchService<T> = SimpleEntitySearchService(entityType, db)
) {

    constructor(
        entityType: KClass<T>,
        db: EntityDB<T> = KeyValueInMemoryEntityDB(),
        searchService: SearchService<T> = SimpleEntitySearchService(entityType, db)
    ): this(entityType.java, db, searchService)

    @GET
    @Produces("text/calendar")
    @Path("/{key}.ics")
    fun getCalendarFeed(
        @PathParam("key") key: String
    ): Response {
        val item = db.get(key)
        if (item == null)
            return Response.status(Response.Status.NOT_FOUND).build()
        return Response.ok(item.toCalendar().toString()).build()
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{key}.json")
    fun getJSON(
        @PathParam("key") key: String
    ): Response {
        val item = db.get(key)
        if (item == null)
            return Response.status(Response.Status.NOT_FOUND).build()
        return Response.ok(item).build()
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/search")
    fun search(
        @QueryParam("q") searchString: String
    ) : Response {
        val searchItems = searchService.search(searchString).take(20)
        return Response.ok(searchItems).build()
    }
}