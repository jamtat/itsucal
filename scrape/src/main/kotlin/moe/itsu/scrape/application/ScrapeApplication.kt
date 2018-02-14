package moe.itsu.scrape.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import moe.itsu.common.model.entity.manga.MangaSeries
import moe.itsu.scrape.api.Scraper
import moe.itsu.scrape.publisher.sevenseas.SevenSeasScraper
import moe.itsu.scrape.publisher.yen.YenPressScraper
import kotlin.reflect.KClass


val om = ObjectMapper()
    .registerKotlinModule()
    .enable(SerializationFeature.INDENT_OUTPUT)


fun main(args: Array<String>) {
    val manager = ScraperManager(
        MangaSeries::class.java,
        {series -> println(om.writeValueAsString(series))}
    )
    val scrapers: ArrayList<KClass<out Scraper<MangaSeries>>> = ArrayList()

    scrapers.add(SevenSeasScraper::class)
    scrapers.add(YenPressScraper::class)

    scrapers.forEach {manager.addScraper(it)}
}