package moe.itsu.scrape.api

import moe.itsu.common.model.entity.Entity
import moe.itsu.common.model.entity.PurchaseInformation

interface PurchaseInformationProvider<T: Entity> {
    fun get(item: T): PurchaseInformation?
}