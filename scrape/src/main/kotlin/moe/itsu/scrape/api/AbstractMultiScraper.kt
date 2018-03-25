package moe.itsu.scrape.api

import moe.itsu.common.model.entity.Entity
import java.util.logging.Logger

abstract class AbstractMultiScraper : MultiScraper {
    var running: Boolean = false
    var logger: Logger = Logger.getLogger(this.javaClass.name)

    override fun run(consumer: (Entity) -> Unit) {
        this.running = true
        logger.info("Started scraper: " + this.javaClass.name)
    }

    override fun stop() {
        this.running = false
        logger.info("Stopped scraper: " + this.javaClass.name)
    }

    override fun updateEntity(entity: Entity): Entity? {
        TODO("not implemented")
    }
}