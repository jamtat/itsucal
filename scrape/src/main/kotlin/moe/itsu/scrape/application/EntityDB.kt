package moe.itsu.scrape.application

import moe.itsu.common.model.entity.Entity

interface EntityDB<T: Entity> {

    val size: Int

    fun add(item: T)

    fun has(item: T): Boolean

    fun delete(item: T): Boolean

    fun clear()

    fun replaceIfPresent(item: T): Boolean

    fun addOrReplace(item: T): Boolean

    fun get(key: String): T?

    fun getAll(): Collection<T>
}