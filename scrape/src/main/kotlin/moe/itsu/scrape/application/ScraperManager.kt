package moe.itsu.scrape.application

import moe.itsu.scrape.api.Scraper
import java.util.*
import java.util.logging.Logger
import kotlin.reflect.KClass

class ScraperManager<T> {

    private val scraperReturnType: Class<T>
    private val db = ItemHashDB<T>()
    private var logger: Logger
    private var scrapers: MutableList<Pair<UUID, Scraper<T>>> = ArrayList()


    constructor(scraperReturnType: Class<T>) {
        this.scraperReturnType = scraperReturnType
        this.logger = Logger.getLogger("scrapermanager:${scraperReturnType.simpleName}")
    }

    fun addScraper(x: Class<out Scraper<T>>) {
        val scraper = x.newInstance()
        val uuid = UUID.randomUUID()
        scrapers.add(Pair(uuid, scraper))
        logger.info("scrapermanager:${scraperReturnType.simpleName}:${scraper.name}:${uuid}:starting")
        scraper.run(onGetItem(uuid, scraper))

    }

    fun addScraper(x: KClass<out Scraper<T>>) = addScraper(x.java)

    @Synchronized
    private fun handleGotItem(item: T) {
        db.addReplace(item)
    }

    private fun onGetItem(uuid: UUID, scraper: Scraper<T>): (T) -> Unit = { item ->
        handleGotItem(item)
        logger.info("scrapermanager:${scraperReturnType.simpleName} db count: ${db.size}")
    }
}