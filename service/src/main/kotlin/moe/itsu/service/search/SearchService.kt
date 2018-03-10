package moe.itsu.service.search

import moe.itsu.common.model.entity.Entity

interface SearchService<T: Entity> {

    data class SearchResult<T: Entity>(
        val item: T,
        val score: Double
    )

    fun search(searchString: String): List<SearchResult<T>>
}