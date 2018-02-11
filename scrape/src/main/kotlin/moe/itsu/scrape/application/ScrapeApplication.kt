package moe.itsu.scrape.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import moe.itsu.common.model.MangaSeries
import moe.itsu.scrape.api.Scraper
import moe.itsu.scrape.publisher.sevenseas.SevenSeasScraper
import moe.itsu.scrape.publisher.yen.YenPressScraper

val om = ObjectMapper()
    .registerKotlinModule()
    .enable(SerializationFeature.INDENT_OUTPUT)

fun printResult(series: MangaSeries) {

}

fun main(args: Array<String>) {
    val scrapers: ArrayList<Scraper<MangaSeries>> = ArrayList()

    scrapers.add(SevenSeasScraper())
    scrapers.add(YenPressScraper())

    scrapers.parallelStream().forEach { it.run { series -> printResult(series) } }
}