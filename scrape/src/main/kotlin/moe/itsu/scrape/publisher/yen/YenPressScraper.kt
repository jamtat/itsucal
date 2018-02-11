package moe.itsu.scrape.publisher.yen

import moe.itsu.common.model.MangaSeries
import moe.itsu.scrape.api.Scraper

class YenPressScraper : Scraper<MangaSeries>() {
    override val name = "yenpress"

    override fun run(consumer: (MangaSeries) -> Unit) {
        TODO("not implemented")
    }

    override fun stop() {
        TODO("not implemented")
    }
}