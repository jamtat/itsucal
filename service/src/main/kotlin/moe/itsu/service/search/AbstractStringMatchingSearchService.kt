package moe.itsu.service.search

import moe.itsu.common.model.entity.Entity

abstract class AbstractStringMatchingSearchService<T: Entity> : SearchService<T> {

    abstract fun getComparisonString(t: T): String

    abstract fun fetchItems(): List<T>

    abstract fun getScore(searchString: String, itemString: String): Double

    override fun search(searchString: String): List<SearchService.SearchResult<T>> =
        fetchItems()
            .map { SearchService.SearchResult(it, getScore(searchString, getComparisonString(it))) }
            .sortedByDescending { it.score }
}