package moe.itsu.common.util

typealias StreamRepeaterHandler<T> = (T) -> T

class StreamRepeater : StreamRepeaterHandler<Any> {

    private val listeners = ArrayList<StreamRepeaterHandler<Any>>()

    override operator fun invoke(x: Any) =  listeners.forEach { it(x) }

    fun add(fn: StreamRepeaterHandler<Any>) = listeners.add(fn)
    fun remove(fn: StreamRepeaterHandler<Any>) = listeners.remove(fn)
}