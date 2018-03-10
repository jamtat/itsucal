package moe.itsu.service.search

import moe.itsu.common.model.entity.Entity
import org.apache.commons.text.similarity.LevenshteinDistance

abstract class AbstractLevenshteinDistanceSearchService<T: Entity> : SearchService<T> {

    val levenshteinDistanceImplementation = LevenshteinDistance()

    abstract fun getComparisonString(t: T): String

    abstract fun fetchItems(): List<T>

    fun getScore(searchString: String, itemString: String): Double =
        1.0 / levenshteinDistanceImplementation.apply(itemString, searchString)

    override fun search(searchString: String): List<SearchService.SearchResult<T>> =
        fetchItems()
            .map { SearchService.SearchResult(it, getScore(searchString, getComparisonString(it))) }
            .sortedByDescending { it.score }
}