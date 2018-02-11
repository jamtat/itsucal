package moe.itsu.common.model

import java.time.LocalDate

data class Manga (
    val name: String,
    val releaseDate: LocalDate,
    val isbn13: ISBN13,
    val format: MangaFormat = MangaFormat.PRINT,
    val pageCount: Int = 0,
    val authors: List<String> = ArrayList(),
    val publisherUrl: String,
    val blurb: String? = null
) {
    override fun equals(other: Any?): Boolean = when(other) {
        is Manga -> isbn13 == other.isbn13
        else -> false
    }

    override fun hashCode(): Int = isbn13.toString().hashCode()
}
