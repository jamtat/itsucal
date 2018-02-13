package moe.itsu.scrape.application

import moe.itsu.common.model.entity.manga.MangaSeries
import moe.itsu.scrape.api.Scraper
import moe.itsu.scrape.publisher.sevenseas.SevenSeasScraper
import moe.itsu.scrape.publisher.yen.YenPressScraper
import kotlin.reflect.KClass


fun main(args: Array<String>) {
    val scrapers: ArrayList<KClass<out Scraper<MangaSeries>>> = ArrayList()

    scrapers.add(SevenSeasScraper::class)
    scrapers.add(YenPressScraper::class)

    scrapers.forEach {ScraperManager(it)}
}