package moe.itsu.scrape.api

import moe.itsu.common.model.entity.Entity
import java.util.logging.Logger

abstract class AbstractPurchaseInformationProvider<T: Entity> : PurchaseInformationProvider<T> {
    var logger: Logger = Logger.getLogger(this.javaClass.name)
}