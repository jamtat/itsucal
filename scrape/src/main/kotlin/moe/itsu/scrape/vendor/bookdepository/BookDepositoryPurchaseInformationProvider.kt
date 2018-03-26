package moe.itsu.scrape.vendor.bookdepository

import moe.itsu.common.model.entity.PurchaseInformation
import moe.itsu.common.model.entity.manga.ISBN13
import moe.itsu.common.model.entity.manga.Manga
import moe.itsu.scrape.api.AbstractPurchaseInformationProvider
import moe.itsu.scrape.util.http.get
import org.jsoup.Jsoup


class BookDepositoryPurchaseInformationProvider : AbstractPurchaseInformationProvider<Manga>() {

    fun getFromISBN(isbn: ISBN13): PurchaseInformation? {
        val urlParams: Map<String, String> = mapOf(
            "searchIsbn" to isbn.toString(),
            "advanced" to "true"
        )
        val response = get(
            url = "https://www.bookdepository.com/search",
            params = urlParams,
            maxAge = 6 * 60 * 60 * 1000 // 6 hour cache time
        )

        if(response.statusCode != 200) {
            return null
        }

        val priceRegex = """[0-9,.]""".toRegex()

        val document = Jsoup.parse(response.text)

        val textPrice = document.selectFirst(".checkout-tools .price .sale-price")
            ?.text()
            ?.filter { it.toString().matches(priceRegex) }

        if(textPrice == null) {
            return null
        }

        val price: Double? = try {
            textPrice.toDouble()
        } catch(e: NumberFormatException) {
            null
        }

        val available = document.select("i.icon-check").size != 0

        return PurchaseInformation(
            price = price,
            available = available,
            url = response.url
        )

    }

    override fun get(item: Manga): PurchaseInformation? {
        return getFromISBN(item.isbn13)
    }
}