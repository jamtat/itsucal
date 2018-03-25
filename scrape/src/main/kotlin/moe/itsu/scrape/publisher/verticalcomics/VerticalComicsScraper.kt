package moe.itsu.scrape.publisher.verticalcomics

import moe.itsu.common.model.entity.Entity
import moe.itsu.common.model.entity.manga.ISBN13
import moe.itsu.common.model.entity.manga.Manga
import moe.itsu.common.model.entity.manga.MangaSeries
import moe.itsu.common.util.findMonth
import moe.itsu.scrape.api.AbstractMultiScraper
import moe.itsu.scrape.api.ScraperException
import moe.itsu.scrape.util.http.get
import org.jsoup.Jsoup
import java.time.DateTimeException
import java.time.LocalDate
import java.util.stream.Collectors

class VerticalComicsScraper : AbstractMultiScraper() {

    private val SERIES_LIST_URL = "http://vertical-comics.com/books.php"

    override val name = "verticalcomics"

    override fun run(consumer: (Entity) -> Unit) {
        super.run(consumer)

        fetchAllSeries(consumer)
    }

    private data class VerticalComicsHomepageMangaItem(
        val series: String,
        val volume: String?,
        val blurb: String,
        val link: String
    )

    private fun fetchAllSeries(consumer: (Entity) -> Unit): List<MangaSeries> {
        logger.info("Fetching all series from $SERIES_LIST_URL")
        val response = get(SERIES_LIST_URL, cache = false)

        if(response.statusCode != 200) {
            throw ScraperException("Could not fetch series from $SERIES_LIST_URL")
        }

        val document = Jsoup.parse(response.text)

        val homepageMangaItems: List<VerticalComicsHomepageMangaItem> =
            document.select("li.item")
                .mapNotNull(fun(element): VerticalComicsHomepageMangaItem? {
                    val series: String? = element.selectFirst("> div > h2")?.text()?.trim()
                    val volume: String? = element.selectFirst("h2 + h4")?.text()?.trim()
                    val blurb: String? = element.selectFirst("p")?.text()?.trim()
                    val link: String? = element.selectFirst("b > a")?.attr("href")?.trim()

                    if(series == null || link == null)
                        return null

                    return VerticalComicsHomepageMangaItem(
                        series,
                        volume,
                        blurb.orEmpty(),
                        link.replace("./", "http://vertical-comics.com/")
                    )
                })

        logger.info("Found ${homepageMangaItems.size} items at $SERIES_LIST_URL")

        val seriesMap = homepageMangaItems.groupBy { it.series }

        return seriesMap.toList()
            .mapNotNull { (name, homepageMangaItems) ->
                val seriesPair = fetchSeriesFromMap(name, homepageMangaItems)
                if(seriesPair !== null) {
                    seriesPair.second.forEach(consumer)
                    consumer(seriesPair.first)
                }
                seriesPair?.first
            }
    }

    private fun fetchSeriesFromMap(
        seriesName: String,
        homepageMangaItems: List<VerticalComicsHomepageMangaItem>
    ): Pair<MangaSeries, List<Manga>>? {
        val items = homepageMangaItems
            .parallelStream()
            .map(::fetchMangaFromHomepageMangaItem)
            .collect(Collectors.toList())
            .filterNotNull()

        return MangaSeries(
            name = seriesName,
            publisher = name,
            items = items.map { it.isbn13 },
            publisherUrl = ""
        ) to items
    }

    private fun fetchMangaFromHomepageMangaItem(item: VerticalComicsHomepageMangaItem): Manga? {

        logger.info("Fetching manga item from ${item.link}")
        val response = get(item.link)

        if(response.statusCode != 200) {
            logger.warning("Could not fetch manga from ${item.link}")
        }

        val document = Jsoup.parse(response.text)

        val aboutPanel = document.selectFirst(".floatRight > .columnHeader")?.parent()

        if(aboutPanel == null)
            return null

        val aboutPanelLines = aboutPanel.wholeText().lines()

        val isbn = aboutPanelLines.find { it.contains("ISBN:") }
            ?.split("ISBN:")?.last()
            ?.trim()
            ?.let { ISBN13(it) }

        if(isbn == null)
            return null

        val pages = aboutPanelLines.find { it.contains("Pages:") }
            ?.split("|")?.first()
            ?.split("Pages:")?.last()
            ?.trim()
            ?.let { it.toInt() }

        val releaseDateString = aboutPanelLines.find { it.contains("On Sale:") }
            ?.split("On Sale:")?.last()
            ?.trim()

        if(releaseDateString == null)
            return null

        val month = findMonth(releaseDateString.split(" ")[0])
        val day = releaseDateString.split(",")[0].split(" ")[1].toInt()
        var year = releaseDateString.split(" ").last().toInt()


        val releaseDate: LocalDate

        try {
            releaseDate = LocalDate.of(year, month, day)
        } catch (err: DateTimeException) {
            logger.warning("Error parsing date from '$releaseDateString' found on ${item.link}")
            return null
        }


        val name = when(item.volume) {
            null -> item.series
            else -> "${item.series} ${item.volume}"
        }

        return Manga(
            name = name,
            publisherUrl = item.link,
            isbn13 = isbn,
            pageCount = pages ?: 0,
            releaseDate = releaseDate
        )
    }
}