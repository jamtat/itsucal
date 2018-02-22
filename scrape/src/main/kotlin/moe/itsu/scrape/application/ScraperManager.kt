package moe.itsu.scrape.application

import moe.itsu.common.model.entity.Entity
import moe.itsu.scrape.api.Scraper
import java.util.UUID
import java.util.logging.Logger
import kotlin.concurrent.thread
import kotlin.reflect.KClass

class ScraperManager<T: Entity>(
    private val scraperReturnType: Class<T>,
    private val consumer: (T) -> Any
) {
    constructor(
        scraperReturnType: KClass<T>,
        consumer: (T) -> Any
    ): this(scraperReturnType.java, consumer)

    private var logger: Logger = Logger.getLogger("scrapermanager:${scraperReturnType.simpleName}")
    private var scrapers: MutableList<Triple<UUID, Thread, Scraper<T>>> = ArrayList()

    fun addScraper(x: Class<out Scraper<T>>) {
        val scraper = x.newInstance()
        val uuid = UUID.randomUUID()

        val thread = thread(start = true){
            logger.info("scrapermanager:${scraperReturnType.simpleName}:${scraper.name}:${uuid} starting in thread ${Thread.currentThread()}")
            scraper.run(onGetItem(uuid, scraper))
        }

        scrapers.add(Triple(uuid, thread, scraper))
    }

    fun addScraper(x: KClass<out Scraper<T>>) = addScraper(x.java)

    @Synchronized
    private fun handleGotItem(item: T) {
        consumer(item)
    }

    private fun onGetItem(uuid: UUID, scraper: Scraper<T>): (T) -> Unit = { item ->
        handleGotItem(item)
        logger.info("scrapermanager:${scraperReturnType.simpleName} item with key: ${item.key}")
    }

    fun reload() {
        val classesToReload: List<Class<out Scraper<T>>> = scrapers.map { (_, _, scraper) -> scraper::class.java }
        scrapers.forEach { (_, thread, _) -> if (thread.isAlive) thread.interrupt() }
        classesToReload.forEach(::addScraper)
    }

    fun clear() {
        logger.info("scrapermanager:${scraperReturnType.simpleName} clearing all scrapers")
        scrapers.forEach { (_, thread, _) -> if (thread.isAlive) thread.interrupt() }
        scrapers.clear()
    }
}