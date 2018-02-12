package moe.itsu.common.model

data class MangaSeries(
    val name: String,
    val publisher: String,
    val items: List<Manga> = ArrayList(),
    val otherNames: List<String> = ArrayList(),
    val publisherUrl: String
) {
    override fun hashCode(): Int = (name + publisher).hashCode()
}
