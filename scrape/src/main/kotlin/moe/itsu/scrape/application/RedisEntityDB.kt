package moe.itsu.scrape.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import moe.itsu.common.model.entity.Entity
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import java.nio.channels.AlreadyConnectedException
import java.util.logging.Logger
import kotlin.reflect.KClass


class RedisEntityDB<T: Entity>(private val entityType: Class<T>) : EntityDB<T> {

    constructor(type: KClass<T>): this(type.java)

    private var logger: Logger = Logger.getLogger("redisentitydb:${entityType.simpleName}")
    private var redisPool: JedisPool? = null
    private val redis: Jedis?
        get() = redisPool?.resource

    private val om = ObjectMapper()
        .registerKotlinModule()

    fun connect(host: String = "localhost", port: Int = 6379): RedisEntityDB<T> {
        if(redis != null)
            throw AlreadyConnectedException()

        redisPool = JedisPool(host, port)
        redis.use {
            val existingKeys = keys()
            logger.info("redisentitydb:${entityType.simpleName} started up, ${existingKeys.size} matching keys present")
        }
        return this
    }

    private val T.redisKey: String
        get() = "entitydb:${entityType.simpleName}:${this.key}"

    private val T.redisJSON: String
        get() = om.writeValueAsString(this)

    private fun keys() = redis?.use {
        it.keys("entitydb:${entityType.simpleName}:*")
    } ?: setOf<String>()

    override val size: Int
        get() = keys().size

    override fun add(item: T) {
        redis?.use {
            it.set(item.redisKey, item.redisJSON)
        }
    }

    override fun has(item: T): Boolean = redis?.use {
           return it.exists(item.redisKey)
        } ?: false

    override fun delete(item: T): Boolean = (redis?.use{
        it.del(item.redisKey)
    } ?: 0) > 0

    override fun clear() {
        redis?.use { it.del(*keys().toTypedArray()) }
    }

    override fun replaceIfPresent(item: T): Boolean {
        return if(has(item)) {
            add(item)
            true
        } else false
    }

    override fun addOrReplace(item: T): Boolean {
        val had = has(item)
        add(item)
        return had
    }

    override fun getAll(): List<T> {
        return keys().mapNotNull(fun(key): T? {
            redis?.use {
                val returnString = it.get(key)
                return om.readValue(returnString, entityType)
            } ?: return null
        })
    }
}