package moe.itsu.service.search

import moe.itsu.common.model.entity.Entity
import moe.itsu.persist.api.EntityDB
import kotlin.reflect.KClass

class SimpleEntitySearchService<T: Entity>(
    private val entityType: Class<T>,
    private val db: EntityDB<T>
): AbstractLevenshteinDistanceSearchService<T>() {

    constructor(
        type: KClass<T>,
        db: EntityDB<T>
    ): this(type.java, db)

    override fun getComparisonString(t: T): String = t.name

    override fun fetchItems(): List<T> = db.getAll().let {
        when(it) {
            is List -> it
            else -> it.toList()
        }
    }

}