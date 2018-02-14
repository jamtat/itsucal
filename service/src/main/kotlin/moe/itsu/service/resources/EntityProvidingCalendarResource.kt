package moe.itsu.service.resources

import moe.itsu.common.model.entity.EntityProvidingCalendar
import moe.itsu.persist.api.EntityDB
import moe.itsu.persist.db.KeyValueInMemoryEntityDB
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import kotlin.reflect.KClass


abstract class EntityProvidingCalendarResource<T: EntityProvidingCalendar>(
    private val entityType: Class<T>,
    private val db: EntityDB<T> = KeyValueInMemoryEntityDB()
) {

    constructor(
        entityType: KClass<T>,
        db: EntityDB<T> = KeyValueInMemoryEntityDB()
    ): this(entityType.java, db)

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
}