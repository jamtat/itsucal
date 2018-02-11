package moe.itsu.scrape.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import moe.itsu.common.model.MangaSeries
import moe.itsu.scrape.publisher.sevenseas.SevenSeasScraper

val om = ObjectMapper()
    .registerKotlinModule()
    .enable(SerializationFeature.INDENT_OUTPUT)

fun printResult(series: MangaSeries) {
//    println("series:${series.publisher}:${series.name}")
}

fun main(args: Array<String>) {
    SevenSeasScraper().run { series -> printResult(series) }
}