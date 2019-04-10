package moe.itsu.service.tasks

import com.google.common.collect.ImmutableMultimap
import io.dropwizard.servlets.tasks.Task
import moe.itsu.common.model.entity.Entity
import moe.itsu.common.model.entity.manga.Manga
import moe.itsu.common.model.entity.manga.MangaSeries
import moe.itsu.persist.db.RedisEntityDB
import moe.itsu.scrape.api.Scraper
import moe.itsu.scrape.application.ScraperManager
import moe.itsu.scrape.publisher.kodansha.KodanshaComicsScraper
import moe.itsu.scrape.publisher.sevenseas.SevenSeasScraper
import moe.itsu.scrape.publisher.verticalcomics.VerticalComicsScraper
import moe.itsu.scrape.publisher.yen.YenPressScraper
import java.io.PrintWriter
import javax.ws.rs.BadRequestException
import kotlin.reflect.KClass

object ScraperTask : Task("scrape") {

    class ScrapeTask(
        scrapers: List<KClass<out Scraper<Entity>>>
    ) {

        private val mangaSeriesDB: RedisEntityDB<MangaSeries> = RedisEntityDB(MangaSeries::class).connect()
        private val mangaDB: RedisEntityDB<Manga> = RedisEntityDB(Manga::class).connect()

        private val manager: ScraperManager<Entity> = ScraperManager(
            Entity::class
        ) { item -> when(item) {
            is MangaSeries -> mangaSeriesDB.add(item)
            is Manga -> mangaDB.add(item)
        }}

        private val scraperMap: Map<String, KClass<out Scraper<Entity>>> = scrapers
            .groupBy { it.java.newInstance().name }
            .mapValues { it.value.first() }

        private val scraperKeys: List<String>
            get() = scraperMap.keys.toList()

        fun scrapeAll(output: PrintWriter) {
            scrape(scraperKeys, output)
        }

        fun scrape(scrapers: List<String>, output: PrintWriter) {
            manager.clear()

            val validScrapers = scrapers.filter { scraperMap.containsKey(it) }
            val scraperClasses = validScrapers.mapNotNull { scraperMap[it] }

            if(validScrapers.isNotEmpty())
                output.write("Starting scrapers: \n${validScrapers.joinToString("\n")}")
            else
                output.write("No valid scraper names provided. Valid options are: \n${scraperMap.keys.joinToString("\n")}")

            scraperClasses.forEach { manager.addScraper( it ) }
        }
    }

    class InvalidEntityTypeException : BadRequestException()

    private val typeMap: Map<String, ScrapeTask> = hashMapOf(
        "mangaseries" to ScrapeTask(
            listOf(
                YenPressScraper::class,
                SevenSeasScraper::class,
                VerticalComicsScraper::class,
                KodanshaComicsScraper::class
            ))
    )

    override fun execute(parameters: ImmutableMultimap<String, String>, output: PrintWriter) {
        val scrapeTask = parameters.get("type")?.first()?.let { typeMap[it] }
            ?: throw InvalidEntityTypeException()

        val scrapers = parameters.get("scrapers")?.toList() ?: emptyList<String>()

        if(scrapers.size == 1 && scrapers[0] == "all") {
            scrapeTask.scrapeAll(output)
        } else {
            scrapeTask.scrape(scrapers, output)
        }
    }
}