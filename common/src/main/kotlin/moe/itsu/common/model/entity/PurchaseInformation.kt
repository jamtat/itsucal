package moe.itsu.common.model.entity

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import moe.itsu.common.util.LocalDateDeserialiser
import moe.itsu.common.util.LocalDateSerialiser
import java.time.LocalDate

data class PurchaseInformation(
    @get:JsonSerialize(using = LocalDateSerialiser::class)
    @param:JsonDeserialize(using = LocalDateDeserialiser::class)
    val releaseDate: LocalDate? = null,
    val url: String? = null,
    val price: Double? = null,
    val available: Boolean? = null,
    val quantity: Int? = null
)