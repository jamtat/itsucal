package moe.itsu.scrape.publisher.kodansha

import moe.itsu.common.model.entity.Entity
import moe.itsu.common.model.entity.manga.ISBN13
import moe.itsu.common.model.entity.manga.Manga
import moe.itsu.common.model.entity.manga.MangaFormat
import moe.itsu.common.model.entity.manga.MangaSeries
import moe.itsu.common.util.findMonth
import moe.itsu.scrape.api.AbstractMultiScraper
import moe.itsu.scrape.api.ScraperException
import moe.itsu.scrape.util.http.get
import org.jsoup.Jsoup
import java.time.DateTimeException
import java.time.LocalDate
import java.util.stream.Collectors

class KodanshaComicsScraper : AbstractMultiScraper() {

    private val SERIES_LIST_URL = "https://kodanshacomics.com/series/"

    override val name = "kodansha"

    override fun run(consumer: (Entity) -> Unit) {
        super.run(consumer)

        fetchBookSeries(consumer)
    }

    override fun updateEntity(entity: Entity): Entity? = when(entity){
        is MangaSeries -> fetchBookSeries(entity.publisherUrl)?.first
        else -> null
    }

    private fun fetchBookSeries(consumer: (Entity) -> Unit): List<MangaSeries> {
        val seriesUrls: List<String> = fetchBookSeriesURLList()

        return seriesUrls
            .parallelStream()
            .map {
                val seriesPair = fetchBookSeries(it)
                if (seriesPair != null) {
                    seriesPair.second.forEach(consumer)
                    consumer(seriesPair.first)
                }
                seriesPair?.first
            }.collect(Collectors.toList())
            .filterNotNull()
    }

    private fun fetchBookSeriesURLList(): List<String> {
        logger.info("Fetching all series from $SERIES_LIST_URL")
        val response = get(SERIES_LIST_URL)

        if(response.statusCode != 200) {
            throw ScraperException("Could not fetch series from $SERIES_LIST_URL")
        }

        val document = Jsoup.parse(response.text)

        return document.select("div.volume > div > a")
            .mapNotNull {it.attr("href")}
    }

    private fun fetchBookSeries(seriesUrl: String): Pair<MangaSeries, List<Manga>>? {
        logger.info("Fetching series from $seriesUrl")
        val response = get(seriesUrl)

        if(response.statusCode != 200) {
            logger.warning("Could not fetch series from $seriesUrl")
            return null
        }

        val container = Jsoup.parse(response.text).selectFirst("#container")

        val seriesName = container.selectFirst("h1")?.text()

        if (seriesName == null) {
            logger.warning("Could not find series name from $seriesUrl")
            return null
        }

        val itemUrls = container
            .select("#volumes + div .volume-listing__image > a")
            .map { it.attr("href") }
            .distinct()
            .distinct()

        val items = itemUrls.mapNotNull(::fetchItemFromUrl)

        return MangaSeries(
            name = seriesName,
            publisher = name,
            items = items.map { it.isbn13 },
            publisherUrl = seriesUrl
        ) to items
    }

    private fun fetchItemFromUrl(itemUrl: String): Manga? {
        logger.info("Fetching item from $itemUrl")
        val response = get(itemUrl)

        if(response.statusCode != 200) {
            logger.warning("Could not fetch series from $itemUrl")
            return null
        }

        val document = Jsoup.parse(response.text)

        val container = document.selectFirst("#container")

        val itemName = container.selectFirst(".volume__title").text()

        var isbn = container.selectFirst("img")
            ?.attr("src")
            ?.split("/")?.last()
            ?.split("-")?.first()
            ?.let { ISBN13(it) }

        var format = MangaFormat.PRINT

        if(isbn != null && !isbn.valid) {
            isbn = document.selectFirst("li.logo--nook")
                ?.attr("href")
                ?.split("ean=")?.last()
                ?.let { ISBN13(it) }

            format = MangaFormat.DIGITAL
        }

        if (isbn == null) {
            logger.warning("Could not parse ISBN from $itemUrl")
            return null
        }

        val pageCount = container.selectFirst("span.volume__meta__page_count")
            ?.text()
            ?.split(" ")?.first()
            ?.let { it.toInt() }

        val releaseDate: LocalDate?

        val releaseDateList = container.selectFirst("span.volume__meta__date_published")
            ?.text()
            ?.split(" ")
        val month = releaseDateList?.get(0)?.let { findMonth( it ) }
        val day = releaseDateList?.get(1)?.toInt()
        val year = releaseDateList?.get(2)?.toInt()

        if(month == null || day == null || year == null) {
            logger.warning("Error parsing date from '$releaseDateList' found on ${itemUrl}")
            return null
        }

        try {
            releaseDate = LocalDate.of(year, month, day)
        } catch (err: DateTimeException) {
            logger.warning("Error parsing date from '$releaseDateList' found on ${itemUrl}")
            return null
        }

        return Manga(
            name = itemName,
            releaseDate = releaseDate ?: LocalDate.MIN,
            isbn13 = isbn,
            format = format,
            pageCount = pageCount ?: 0,
            publisherUrl = itemUrl
        )
    }
}