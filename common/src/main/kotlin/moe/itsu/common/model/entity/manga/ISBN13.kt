package moe.itsu.common.model.entity.manga

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializable
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.jsontype.TypeSerializer


class ISBN13(code: String) : JsonSerializable {
    override fun serializeWithType(gen: JsonGenerator?, serializers: SerializerProvider?, typeSer: TypeSerializer?) {
        serialize(gen, serializers)
    }

    override fun serialize(gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeString(code)
    }

    val ISBN_ALLOWED_CHARS = Regex("[0-9]")

    private val code: String = code.filter { ISBN_ALLOWED_CHARS.matches(it.toString()) }

    override fun toString(): String = code

    override fun equals(other: Any?): Boolean = other.toString() == toString()
}