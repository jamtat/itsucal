package moe.itsu.common.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import java.nio.channels.AlreadyConnectedException
import java.util.logging.Logger

object RedisSettingsProvider : SettingsProvider {

    private var logger: Logger = Logger.getLogger("redissettingsprovider")
    private var redisPool: JedisPool? = null
    private val redis: Jedis?
        get() = redisPool?.resource

    private val om = ObjectMapper()
        .registerKotlinModule()

    fun connect(host: String = "localhost", port: Int = 6379) {
        if(redis != null)
            throw AlreadyConnectedException()

        redisPool = JedisPool(host, port)
        redis.use {
            logger.info("redissettingsprovider started up")
        }
    }

    init {
        connect()
    }

    private val String.redisKey
        get() ="itsucalsetting:$this"

    override fun get(settingName: String): String? {
        redis?.use {
            return if(it.exists(settingName.redisKey))
                it.get(settingName.redisKey)
            else null
        } ?: return null
    }

    override fun set(settingName: String, value: String) {
        redis?.use {
            it.set(settingName.redisKey, value)
        }
    }
}