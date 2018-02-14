package moe.itsu.common.model.entity

import moe.itsu.common.model.calendar.Calendar

interface EntityProvidingCalendar: Entity {
    fun toCalendar(): Calendar
}