package moe.itsu.scrape.application

class ItemHashDB<T> {
    private var store: MutableSet<T> = HashSet()

    val size: Int
        get() = store.size

    fun add(item: T) = store.add(item)

    fun has(item: T): Boolean = store.contains(item)

    fun delete(item: T): Boolean {
        if(has(item)) {
            store.remove(item)
            return true
        }
        return false
    }

    fun clear() {
        store = HashSet()
    }

    fun replace(item: T): Boolean {
        if(has(item)) {
            delete(item)
            add(item)
            return true
        }
        return false
    }

    fun addReplace(item: T): Boolean {
        val had = has(item)
        if(had)
            delete(item)
        add(item)
        return had
    }

    fun getAll(): Set<T> = store.toSet()
}