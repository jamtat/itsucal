package moe.itsu.service.application

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import moe.itsu.service.resources.HealthCheckResource
import moe.itsu.service.resources.MangaSeriesResource

fun main(args: Array<String>) {
    ItsucalApplication().run(*args)
}

class ItsucalApplication : Application<ItsucalApplicationConfiguration>() {

    override fun run(
        configuration: ItsucalApplicationConfiguration,
        environment: Environment
    ) {
        val mangaServiesResource = MangaSeriesResource()
        environment.jersey().register(mangaServiesResource)
        environment.healthChecks().register("noice", HealthCheckResource())
    }

    override fun initialize(bootstrap: Bootstrap<ItsucalApplicationConfiguration>) {
        bootstrap.objectMapper.registerKotlinModule()
    }
}