package moe.itsu.common.model.entity.manga

import moe.itsu.common.model.entity.Entity
import moe.itsu.common.util.prepareKeyString

data class MangaSeries(
    override val name: String,
    val publisher: String,
    val items: List<ISBN13> = ArrayList(),
    val otherNames: List<String> = ArrayList(),
    val publisherUrl: String
) : Entity {
    override val key: String
        get() = "$publisher:${prepareKeyString(name)}"

    companion object {
        val version = 1
    }
}
