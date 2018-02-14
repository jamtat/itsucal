package moe.itsu.scrape.util

typealias StreamRepeaterHandler<T> = (T) -> Any

class StreamRepeater<T> : StreamRepeaterHandler<T> {

    private val listeners = ArrayList<StreamRepeaterHandler<T>>()

    override operator fun invoke(x: T) = listeners.forEach { it(x) }

    fun add(fn: StreamRepeaterHandler<T>) = listeners.add(fn)
    fun remove(fn: StreamRepeaterHandler<T>) = listeners.remove(fn)
}