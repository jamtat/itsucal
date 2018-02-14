package moe.itsu.common.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(value = ["key"])
interface Entity {
    val name: String
    val key: String

    companion object {
        val version: Int = 1
    }
}