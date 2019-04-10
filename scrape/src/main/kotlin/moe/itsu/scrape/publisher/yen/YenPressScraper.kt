package moe.itsu.scrape.publisher.yen

import moe.itsu.common.model.entity.Entity
import moe.itsu.common.model.entity.manga.ISBN13
import moe.itsu.common.model.entity.manga.Manga
import moe.itsu.common.model.entity.manga.MangaFormat
import moe.itsu.common.model.entity.manga.MangaSeries
import moe.itsu.scrape.api.AbstractMultiScraper
import moe.itsu.scrape.api.ScraperException
import moe.itsu.scrape.util.http.get
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.util.stream.Collectors

class YenPressScraper : AbstractMultiScraper() {

    companion object {
        private const val SERIES_LIST_URL = "https://yenpress.com/books/"
        private const val TITLE_BASE_URL = "https://yenpress.com"
    }

    override val name = "yenpress"

    override fun run(consumer: (Entity) -> Unit) {
        super.run(consumer)

        fetchBookSeries(consumer)
    }

    override fun updateEntity(entity: Entity): Entity? = when(entity) {
        is MangaSeries -> fetchBookSeries(entity.publisherUrl)?.first
        is Manga -> fetchDetailForItem(entity.publisherUrl)
        else -> null
    }

    private fun fetchBookSeries(consumer: (Entity) -> Unit): List<MangaSeries> {
        // We don't compose the MangaSeries here, because their
        // series page spells some titles wrong.
        // In particular they misspell Today's Cerberus which
        // is my favourite title so warrants getting titles
        // from the detail page

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
        val response = get(SERIES_LIST_URL, cache = false)

        if(response.statusCode != 200) {
            throw ScraperException("Could not fetch series from $SERIES_LIST_URL")
        }

        val list = ArrayList<String>()

        Jsoup.parse(response.text).run {
            select(".series-listing").forEach(fun(element) {
                val seriesName: String = element.selectFirst(".series-title").text()
                val publisherUrl: String? = element.selectFirst(".series-title a")
                    ?.attr("href")
                    ?.replace("""\s""".toRegex(), "")

                if(
                    publisherUrl == null ||
                    seriesName.toLowerCase().contains("(novel)") ||
                    seriesName.toLowerCase().contains("(light novel)") ||
                    seriesName.toLowerCase().contains("kingdom hearts") // Fickle u
                ) {
                    return
                }

                list.add(publisherUrl)
            })
        }

        return list
    }

    private fun fetchBookSeries(seriesUrl: String): Pair<MangaSeries, List<Manga>>? {
        logger.info("Fetching series from $seriesUrl")
        val response = get(seriesUrl)

        if(response.statusCode != 200) {
            logger.warning("Could not fetch series from $seriesUrl")
            return null
        }

        val document = Jsoup.parse(response.text)

        val seriesNameElement: Element? = document.selectFirst(".hentry header")

        if (seriesNameElement == null) {
            logger.warning("Could not get series name for $seriesUrl, may be a one off and not a series")
            return null
        }

        val seriesName = seriesNameElement.text().trim()

        val items = document.select(".book-detail")
            .parallelStream()
            .map(fun(element): Manga? {
                val href = element.select(".book-format-links a")
                    .find { !it.text().toLowerCase().contains("electronic") }
                    ?.attr("href")

                if(href == null) {
                    logger.warning("Could not find non ebook url for ${element.selectFirst("h1").text()} on $seriesUrl")
                    return null
                }

                return fetchDetailForItem(TITLE_BASE_URL + href)
            }).collect(Collectors.toList())
            .filterNotNull()

        logger.info("Fetched series \"$seriesName\" from $seriesUrl, found ${items.size} items")

        if (items.isEmpty())
            return null

        return MangaSeries(
            name = seriesName,
            publisher = name,
            items = items.map { it.isbn13 },
            publisherUrl = seriesUrl
        ) to items
    }

    private fun fetchDetailForItem(itemUrl: String): Manga? {
        logger.info("Fetching item from $itemUrl")
        val response = get(itemUrl)

        if(response.statusCode != 200) {
            logger.warning("Could not fetch series from $itemUrl")
            return null
        }

        val document = Jsoup.parse(response.text)

        val itemName = document.selectFirst("h2#book-title").text().trim()
        val isbn = document.select("#book-details > ul > li")
            .find { it.text().contains("ISBN") }
            ?.selectFirst(".detail-value")
            ?.text()

        val authors: List<String> = document.selectFirst("h3#book-author")
            ?.text()
            ?.trim()
            ?.replace("^By ".toRegex(), "")
            ?.replace("\\(.*\\)\$".toRegex(), "")
            ?.split(",")
            ?.map { it.trim() } ?: ArrayList()

        val blurbElement: Element? = document.selectFirst("#book-description-full")

        val blurb = blurbElement?.text()?.trim()

        if (isbn == null) {
            logger.warning("Could not parse ISBN from $itemUrl (${response.text})")
            return null
        }

        val date: LocalDate?

        try {
            val dateMatch = Regex("""\d{2}/\d{2}/\d{4}""")
                .find(document.select("#book-details").text())?.value
            if(dateMatch != null) {
                val (month, day, year) = dateMatch.split("/").map { it.toInt() }
                date = LocalDate.of(year, month, day)
            } else {
                logger.warning("Could not parse date from $itemUrl (${response.text})")
                return null
            }
        } catch (e: NumberFormatException) {
            logger.warning("Could not parse date from $itemUrl (${response.text})")
            return null
        } catch (e: NoSuchElementException) {
            logger.warning("Could not parse date from $itemUrl (${response.text})")
            return null
        }

        return Manga(
            name = itemName,
            releaseDate = date,
            isbn13 = ISBN13(isbn),
            format = MangaFormat.PRINT,
            publisherUrl = itemUrl,
            blurb = blurb,
            authors = authors
        )
    }

    override fun stop() {
        TODO("not implemented")
    }
}