package moe.itsu.scrape.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import moe.itsu.common.model.entity.manga.MangaSeries
import moe.itsu.scrape.api.Scraper
import moe.itsu.scrape.publisher.sevenseas.SevenSeasScraper
import moe.itsu.scrape.publisher.yen.YenPressScraper
import moe.itsu.scrape.util.StreamRepeater
import java.io.BufferedWriter
import java.io.FileWriter
import java.util.*
import kotlin.reflect.KClass


val prettyOm = ObjectMapper()
    .registerKotlinModule()
    .enable(SerializationFeature.INDENT_OUTPUT)

val om = ObjectMapper()
    .registerKotlinModule()


fun main(args: Array<String>) {
    val repeater = StreamRepeater<Any>()
    val writer = BufferedWriter(FileWriter("${System.getProperty("user.dir")}/out-${UUID.randomUUID()}.out", true))
    repeater.add {series -> println(prettyOm.writeValueAsString(series))}
    repeater.add {series ->
        writer.write(om.writeValueAsString(series) + "\n")
        writer.flush()
    }

    val manager = ScraperManager(
        MangaSeries::class,
        repeater
    )

    val scrapers: ArrayList<KClass<out Scraper<MangaSeries>>> = ArrayList()

    scrapers.add(SevenSeasScraper::class)
    scrapers.add(YenPressScraper::class)

    scrapers.forEach {manager.addScraper(it)}
}