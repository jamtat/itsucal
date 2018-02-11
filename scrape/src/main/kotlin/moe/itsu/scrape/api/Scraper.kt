package moe.itsu.scrape.api

import java.util.logging.Logger

abstract class Scraper<T> {
    var running: Boolean = false
    var logger: Logger = Logger.getLogger(this.javaClass.name)
    abstract val name: String

    open fun run(consumer: (T) -> Unit) {
        this.running = true
        logger.info("Started scraper: " + this.javaClass.name)
    }

    open fun stop() {
        this.running = false
        logger.info("Stopped scraper: " + this.javaClass.name)
    }
}