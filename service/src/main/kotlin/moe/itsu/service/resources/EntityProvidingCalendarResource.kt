package moe.itsu.service.resources

import moe.itsu.common.model.calendar.Calendar
import moe.itsu.common.model.entity.Entity
import moe.itsu.persist.api.EntityDB
import moe.itsu.persist.db.KeyValueInMemoryEntityDB
import moe.itsu.service.search.SearchService
import moe.itsu.service.search.SimpleEntitySearchService
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import kotlin.reflect.KClass


abstract class EntityProvidingCalendarResource<T: Entity>(
    protected val entityType: Class<T>,
    protected val db: EntityDB<T> = KeyValueInMemoryEntityDB(),
    protected val searchService: SearchService<T> = SimpleEntitySearchService(entityType, db)
) {

    constructor(
        entityType: KClass<T>,
        db: EntityDB<T> = KeyValueInMemoryEntityDB(),
        searchService: SearchService<T> = SimpleEntitySearchService(entityType, db)
    ): this(entityType.java, db, searchService)

    abstract fun toCalendar(item: T): Calendar

    open fun composeItem(item: T): Any {
        return item
    }

    @GET
    @Produces("text/calendar")
    @Path("/{key}.ics")
    fun getCalendarFeed(
        @PathParam("key") key: String
    ): Response {
        val item = db.get(key)
        if (item == null)
            return Response.status(Response.Status.NOT_FOUND).build()
        return Response.ok(toCalendar(item).toString()).build()
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
        return Response.ok(composeItem(item)).build()
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/dump")
    fun dump(): Response {
        val allItems = db.getAll()
        val keyed = allItems.groupBy { it.key }
        return Response.ok(object {
            val type = entityType.simpleName
            val items = keyed
                .mapValues { composeItem(it.value.first()) }
        }).build()
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/count")
    fun count(): Response {
        return Response.ok(db.size).build()
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/list")
    fun list(): Response {
        return Response.ok(db.getAll().map { it.key }.toList()).build()
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