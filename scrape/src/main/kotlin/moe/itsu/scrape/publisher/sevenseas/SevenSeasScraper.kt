package moe.itsu.scrape.publisher.sevenseas

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import moe.itsu.common.model.ISBN13
import moe.itsu.common.model.Manga
import moe.itsu.common.model.MangaFormat
import moe.itsu.common.model.MangaSeries
import moe.itsu.scrape.api.Scraper
import org.jsoup.Jsoup
import java.time.LocalDate

class SevenSeasScraper : Scraper<MangaSeries>() {

    private val SERIES_LIST_URL = "http://www.sevenseasentertainment.com/series-list/"

    override val name = "sevenseas"

    override fun run(consumer: (MangaSeries) -> Unit) {
        super.run(consumer)

        fetchSeriesList()
            .parallelStream()
            .forEach { series ->
                val seriesItems = fetchItemsForSeries(series)
                if (seriesItems != null)
                    consumer(series.copy(items = seriesItems))
            }
    }

    private fun fetchSeriesList(): List<MangaSeries> {
        val list = ArrayList<MangaSeries>()

        logger.info("Fetching all series from $SERIES_LIST_URL")
        val (_, _, result) = Fuel.get(SERIES_LIST_URL).responseString()

        when (result) {
            is Result.Failure -> {
                logger.warning("Could not fetch series from $SERIES_LIST_URL")
            }
            is Result.Success -> Jsoup.parse(result.get()).run {
                select("tr#volumes").forEach { element ->
                    val seriesName: String = element.selectFirst("strong").text()
                    val publisherUrl: String = element.selectFirst("td > a").attr("href")

                    val series = MangaSeries(
                        name = seriesName,
                        publisher = name,
                        publisherUrl = publisherUrl
                    )

                    if (!seriesName.toLowerCase().contains("light novel"))
                        list.add(series)
                }
            }
        }

        return list
    }

    private fun fetchItemsForSeries(series: MangaSeries): List<Manga>? {
        logger.info("Fetching series \"${series.name}\" from ${series.publisherUrl}")
        val (_, _, result) = Fuel.get(series.publisherUrl).responseString()

        if(result is Result.Failure) {
            logger.warning("Could not fetch series \"${series.name}\" from ${series.publisherUrl}")
            return null
        }

        val document = Jsoup.parse(result.get())

        val items: List<Manga> = document.select(".series-volume")
            .mapNotNull(fun(element): Manga? {
            val txt = element.text()

            try {
                val dateMatch = Regex("""\d{4}/\d{2}/\d{2}""").find(txt)?.value

                if(dateMatch != null) {
                    val (year, month, day) = dateMatch.split("/").map { it.toInt() }
                    val isbn = txt.split("ISBN:").last().trim()
                    val authors = document.select("#series-meta a[href*='/creator/']").map { it.text().trim() }

                    return Manga(
                        name = element.selectFirst("h3").text().trim(),
                        releaseDate = LocalDate.of(year,month,day),
                        publisherUrl = element.selectFirst("a").attr("href"),
                        isbn13 = ISBN13(isbn),
                        format = MangaFormat.PRINT,
                        authors = authors
                    )
                } else {
                    logger.warning("Could not parse date from ${series.publisherUrl} ($txt)")
                    return null
                }

            } catch (e: NumberFormatException) {
                logger.warning("Could not parse date from ${series.publisherUrl} ($txt)")
                return null
            } catch (e: NoSuchElementException) {
                logger.warning("Could not parse ISBN from ${series.publisherUrl} ($txt)")
                return null
            }
        })

        logger.info("Fetched series \"${series.name}\" from ${series.publisherUrl}, found ${items.size} items")

        return items

    }

    override fun stop() {
        TODO("not implemented")
    }
}