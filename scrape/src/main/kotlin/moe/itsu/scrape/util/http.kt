package moe.itsu.scrape.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import moe.itsu.common.util.RedisSettingsProvider
import java.io.File
import java.util.*
import java.util.logging.Logger
import kotlin.collections.HashMap

object http {

    var logger: Logger = Logger.getLogger(this.javaClass.name)
    val om = ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .registerKotlinModule()

    data class Response(
        val text: String,
        val statusCode: Int,
        val url: String
    )

    data class HTTPCacheEntry(
        val uuid: String,
        val timestamp: Long = Long.MIN_VALUE
    )

    var cacheMap = HashMap<String, HTTPCacheEntry>()

    private val CACHE_FOLDER = System.getProperty("user.home") + "/.itsucal/scrape/httpcache/"
    private val INDEX_FILE = CACHE_FOLDER + "_cache-db.json"
    private val CACHE_ENABLED: Boolean
        get() = RedisSettingsProvider.get("httpscrapecacheenabled") == "1"

    init {
        File(CACHE_FOLDER).mkdirs()
        val indexfile = File(INDEX_FILE)
        if(indexfile.exists()) {
            val txt = indexfile.readText()
            try {
                cacheMap = om.readValue(txt)
            } catch (err: Exception) {
                logger.warning("Couldn't read http cache index file")
                File(INDEX_FILE + ".broken").writeText(txt)
                cacheMap = HashMap()
            }
        } else {
            writeCacheFile()
        }
    }

    fun get(
        url: String,
        params: Map<String, String> = mapOf(),
        cache: Boolean = CACHE_ENABLED,
        maxAge: Long = Long.MAX_VALUE
    ): Response =
        if (cache)
            cachedGet(url, params, maxAge)
        else
            uncachedGet(url, params)

    private fun cachedGet(
        url: String,
        params: Map<String, String> = mapOf(),
        maxAge: Long = Long.MAX_VALUE
    ): Response {
        val requestId = om.writeValueAsString(object {
            val url = url
            val params = params
        })

        val now = System.currentTimeMillis()
        val cachedResponse: Response? = cacheMap[requestId]?.let { entry ->
            if(entry.timestamp > now - maxAge) getCachedResponse(entry.uuid)
            else null
        }

        if(cachedResponse != null) {
            return cachedResponse
        }

        val uuid = UUID.randomUUID().toString()
        val response = uncachedGet(url, params)
        putCachedResponse(requestId, uuid, response)
        return response
    }

    private fun getCachedResponse(uuid: String): Response? {
        return try {
            val fileString = File("$CACHE_FOLDER$uuid.json").readText()
            om.readValue(fileString)
        } catch (err: Exception) {
            null
        }
    }

    @Synchronized
    private fun putCachedResponse(requestId: String, uuid: String, response: Response) {
        File("$CACHE_FOLDER$uuid.json").writeText(om.writeValueAsString(response))
        cacheMap[requestId] = HTTPCacheEntry(uuid, System.currentTimeMillis())
        writeCacheFile()
    }


    @Synchronized
    private fun writeCacheFile() {
        File(INDEX_FILE).writeText(om.writeValueAsString(cacheMap))
    }

    private fun uncachedGet(url: String, params: Map<String, String> = mapOf()): Response {
        val response = khttp.get(url, params = params)
        return Response(
            response.text,
            response.statusCode,
            response.url
        )
    }
}