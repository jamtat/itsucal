package moe.itsu.persist.db

import moe.itsu.common.model.entity.Entity
import moe.itsu.persist.api.EntityDB

class KeyValueInMemoryEntityDB<T : Entity> : EntityDB<T> {

    private val map = HashMap<String, T>()

    override val size: Int
        get() = map.size

    override fun add(item: T) = map.set(item.key, item)

    override fun has(item: T): Boolean = map.containsKey(item.key)

    override fun delete(item: T): Boolean {
        if(has(item)) {
            map.remove(item.key)
            return true
        }
        return false
    }

    override fun clear() = map.clear()

    override fun replaceIfPresent(item: T): Boolean {
        return if(has(item)) {
            add(item)
            true
        } else false
    }

    override fun addOrReplace(item: T): Boolean {
        val had = has(item)
        add(item)
        return had
    }

    override fun get(key: String): T? = map[key]

    override fun getAll(): Collection<T> = map.values
}