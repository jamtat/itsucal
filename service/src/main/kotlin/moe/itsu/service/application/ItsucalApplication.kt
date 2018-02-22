package moe.itsu.service.application

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import moe.itsu.service.resources.HealthCheckResource
import moe.itsu.service.resources.MangaSeriesResource
import moe.itsu.service.tasks.ScraperTask

fun main(args: Array<String>) {
    ItsucalApplication().run(*args)
}

class ItsucalApplication : Application<ItsucalApplicationConfiguration>() {

    override fun run(
        configuration: ItsucalApplicationConfiguration,
        environment: Environment
    ) {
        environment.jersey().register(MangaSeriesResource())

        environment.admin().addTask(ScraperTask)

        environment.healthChecks().register("noice", HealthCheckResource())
    }

    override fun initialize(bootstrap: Bootstrap<ItsucalApplicationConfiguration>) {
        bootstrap.objectMapper.registerKotlinModule()
    }
}