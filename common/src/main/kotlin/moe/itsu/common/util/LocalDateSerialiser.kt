package moe.itsu.common.util

import com.fasterxml.jackson.core.JsonProcessingException
import java.io.IOException
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class LocalDateSerialiser : StdSerializer<LocalDate>(LocalDate::class.java) {

    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(value: LocalDate, gen: JsonGenerator, sp: SerializerProvider) {
        gen.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE))
    }

    companion object {

        private val serialVersionUID = 1L
    }
}