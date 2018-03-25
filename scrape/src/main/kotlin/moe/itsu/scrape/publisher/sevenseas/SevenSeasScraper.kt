package moe.itsu.scrape.publisher.sevenseas

import moe.itsu.common.model.entity.Entity
import moe.itsu.common.model.entity.manga.ISBN13
import moe.itsu.common.model.entity.manga.Manga
import moe.itsu.common.model.entity.manga.MangaFormat
import moe.itsu.common.model.entity.manga.MangaSeries
import moe.itsu.scrape.api.AbstractMultiScraper
import moe.itsu.scrape.api.ScraperException
import moe.itsu.scrape.util.http.get
import org.jsoup.Jsoup
import java.time.LocalDate
import java.util.stream.Collectors

class SevenSeasScraper : AbstractMultiScraper() {

    private val SERIES_LIST_URL = "http://www.sevenseasentertainment.com/series-list/"

    override val name = "sevenseas"

    override fun run(consumer: (Entity) -> Unit) {
        super.run(consumer)

        fetchAllSeries(consumer)
    }

    override fun updateEntity(entity: Entity): Entity? = when(entity) {
        is MangaSeries -> fetchSeries(entity.publisherUrl)?.first
        else -> null
    }

    private fun fetchAllSeries(consumer: (Entity) -> Unit): List<MangaSeries> {
        val urlList = ArrayList<String>()

        logger.info("Fetching all series from $SERIES_LIST_URL")
        val response = get(SERIES_LIST_URL, cache = false)

        if(response.statusCode != 200) {
            throw ScraperException("Could not fetch series from $SERIES_LIST_URL")
        }

        Jsoup.parse(response.text).run {
            select("tr#volumes").forEach { element ->
                val seriesName: String = element.selectFirst("strong").text()
                val publisherUrl: String = element.selectFirst("td > a").attr("href")

                if (!seriesName.toLowerCase().contains("light novel"))
                    urlList.add(publisherUrl)
            }
        }

        return urlList
            .parallelStream()
            .map {
                val seriesPair = fetchSeries(it)
                if(seriesPair != null) {
                    seriesPair.second.forEach(consumer)
                    consumer(seriesPair.first)
                }
                seriesPair?.first
            }.collect(Collectors.toList())
            .filterNotNull()
    }

    private fun fetchSeries(seriesUrl: String): Pair<MangaSeries, List<Manga>>? {
        logger.info("Fetching series from $seriesUrl")
        val response = get(seriesUrl)

        if(response.statusCode != 200) {
            logger.warning("Could not fetch series from $seriesUrl")
            return null
        }

        val document = Jsoup.parse(response.text)

        val seriesName: String? = document.selectFirst("h2.topper")
            ?.text()
            ?.split(":")
            ?.last()
            ?.trim()

        if(seriesName == null) {
            logger.warning("Could not find series name from $seriesUrl")
            return null
        }

        val originalTitle: String? = document.selectFirst("#originaltitle")
            ?.text()
            ?.trim()

        val otherNames: List<String> = when(originalTitle) {
            null -> ArrayList()
            else -> listOf(originalTitle)
        }

        val items: List<Manga> = document.select(".series-volume")
            .parallelStream()
            .map(fun(element): Manga? {
                val txt = element.text()

                try {
                    val dateMatch = Regex("""\d{4}/\d{2}/\d{2}""").find(txt)?.value

                    if(dateMatch != null) {
                        val (year, month, day) = dateMatch.split("/").map { it.toInt() }
                        val isbn = txt.split("ISBN:").last().trim()
                        val authors = document.select("#series-meta a[href*='/creator/']").map { it.text().trim() }

                        return Manga(
                            name = element.selectFirst("h3").text().trim(),
                            releaseDate = LocalDate.of(year, month, day),
                            publisherUrl = element.selectFirst("a").attr("href"),
                            isbn13 = ISBN13(isbn),
                            format = MangaFormat.PRINT,
                            authors = authors
                        )
                    } else {
                        logger.warning("Could not parse date from $seriesUrl ($txt)")
                        return null
                    }

                } catch (e: NumberFormatException) {
                    logger.warning("Could not parse date from $seriesUrl ($txt)")
                    return null
                } catch (e: NoSuchElementException) {
                    logger.warning("Could not parse ISBN from $seriesUrl ($txt)")
                    return null
                }
            }).collect(Collectors.toList())
            .filterNotNull()

        logger.info("Fetched series \"$seriesName\" from $seriesUrl, found ${items.size} items")

        return MangaSeries(
            name = seriesName,
            publisher = name,
            items = items.map { it.isbn13 },
            publisherUrl = seriesUrl,
            otherNames = otherNames
        ) to items
    }
}