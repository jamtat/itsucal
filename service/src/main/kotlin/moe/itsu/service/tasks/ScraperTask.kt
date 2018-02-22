package moe.itsu.service.tasks

import com.google.common.collect.ImmutableMultimap
import io.dropwizard.servlets.tasks.Task
import moe.itsu.common.model.entity.Entity
import moe.itsu.common.model.entity.manga.MangaSeries
import moe.itsu.persist.db.RedisEntityDB
import moe.itsu.scrape.api.Scraper
import moe.itsu.scrape.application.ScraperManager
import moe.itsu.scrape.publisher.sevenseas.SevenSeasScraper
import moe.itsu.scrape.publisher.verticalcomics.VerticalComicsScraper
import moe.itsu.scrape.publisher.yen.YenPressScraper
import java.io.PrintWriter
import javax.ws.rs.BadRequestException
import kotlin.reflect.KClass

object ScraperTask : Task("scrape") {

    class ScrapeTask<T: Entity>(
        type: Class<T>,
        scrapers: List<Class<out Scraper<T>>>
    ) {
        constructor(
            type: KClass<T>,
            scrapers: List<KClass<out Scraper<T>>>
        ): this(type.java, scrapers.map {it.java})

        private val db: RedisEntityDB<T> = RedisEntityDB(type).connect()

        private val manager: ScraperManager<T> = ScraperManager(
            type,
            {item -> db.add(item)}
        )

        private val scraperMap: Map<String, Class<out Scraper<T>>> = scrapers
            .groupBy { it.newInstance().name }
            .mapValues { it.value.first() }

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

    private val typeMap: Map<String, ScrapeTask<out Entity>> = hashMapOf(
        "mangaseries" to ScrapeTask(
            MangaSeries::class,
            listOf(
                YenPressScraper::class,
                SevenSeasScraper::class,
                VerticalComicsScraper::class
            ))
    )

    override fun execute(parameters: ImmutableMultimap<String, String>, output: PrintWriter) {
        val scrapeTask = parameters.get("type")?.first()?.let { typeMap[it] }
            ?: throw InvalidEntityTypeException()

        scrapeTask.scrape(parameters.get("scrapers").toList(), output)
    }
}