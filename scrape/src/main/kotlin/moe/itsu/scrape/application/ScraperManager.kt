package moe.itsu.scrape.application

import moe.itsu.scrape.api.Scraper
import java.util.*
import java.util.logging.Logger
import kotlin.reflect.KClass

class ScraperManager<T> {

    private val db = ItemHashDB<T>()
    private var logger: Logger
    private var scraper: Scraper<T>
    private var uuid = UUID.randomUUID()
    private var scraperClass: Class<out Scraper<T>>

    constructor(x: Class<out Scraper<T>>) {
        scraperClass = x
        scraper = x.newInstance()
        logger = Logger.getLogger("scrapermanager:${scraper.name}:${uuid}")
        scraper.run(::onGetItem)
    }

    constructor(x: KClass<out Scraper<T>>): this(x.java)

    private fun onGetItem(item: T) {
        db.addReplace(item)
        logger.info("${scraper.name}:${uuid} db count: ${db.size}")
    }

    fun updateItem(item: T): T? {
        val new = scraper.updateEntity(item)
        if(new != null) {
            onGetItem(new)
        }

        return new
    }

    fun getAll(): Collection<T> = db.getAll()

    fun reload() {
        db.clear()
        scraper.stop()
        scraper = scraperClass.newInstance()
        scraper.run(::onGetItem)
    }
}