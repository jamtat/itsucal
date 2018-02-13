package moe.itsu.common.model.entity

import moe.itsu.common.model.calendar.Calendar

interface EntityProvidingCalendar {
    fun toCalendar(): Calendar
}