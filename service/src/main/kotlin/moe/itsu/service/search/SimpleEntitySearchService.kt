package moe.itsu.service.search

import moe.itsu.common.model.entity.Entity
import moe.itsu.persist.api.EntityDB
import org.apache.commons.text.similarity.LevenshteinDistance
import kotlin.reflect.KClass

class SimpleEntitySearchService<T: Entity>(
    private val entityType: Class<T>,
    private val db: EntityDB<T>
): AbstractStringMatchingSearchService<T>() {

    constructor(
        type: KClass<T>,
        db: EntityDB<T>
    ): this(type.java, db)

    val comparator = LevenshteinDistance()

    override fun getComparisonString(t: T): String = t.name

    override fun fetchItems(): List<T> = db.getAll().let {
        when(it) {
            is List -> it
            else -> it.toList()
        }
    }

    override fun getScore(searchString: String, itemString: String): Double =
        1.0 / comparator.apply(searchString, itemString)

}