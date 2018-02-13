package moe.itsu.scrape.api

interface Scraper<T> {

    val name: String

    fun run(consumer: (T) -> Unit)

    fun stop()

    fun updateEntity(entity: T): T?
}