package moe.itsu.service.resources

import com.codahale.metrics.health.HealthCheck

class HealthCheckResource : HealthCheck() {
    override fun check(): Result = Result.healthy("げんきです！")
}