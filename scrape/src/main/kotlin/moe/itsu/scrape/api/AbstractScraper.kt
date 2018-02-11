package moe.itsu.scrape.api

import java.util.logging.Logger

abstract class AbstractScraper<T> : Scraper<T> {
    var running: Boolean = false
    var logger: Logger = Logger.getLogger(this.javaClass.name)
    abstract val name: String

    override fun run(consumer: (T) -> Unit) {
        this.running = true
        logger.info("Started scraper: " + this.javaClass.name)
    }

    override fun stop() {
        this.running = false
        logger.info("Stopped scraper: " + this.javaClass.name)
    }

    override fun updateEntity(entity: T): T? {
        TODO("not implemented")
    }
}