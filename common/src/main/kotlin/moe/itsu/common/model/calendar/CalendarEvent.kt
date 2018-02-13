package moe.itsu.common.model.calendar

import java.time.LocalDate

data class CalendarEvent (
    val date: LocalDate,
    val name: String
) {
    override fun toString(): String {

        val validChars = "[0-9A-ZA-z_]".toRegex()

        val uidName = name.filter {it.toString().matches(validChars)}

        val yyyy = date.year.toString()
        val mm = date.month.value.toString().padStart(2, '0')
        val dd = date.dayOfMonth.toString().padStart(2, '0')

        val nextDate = date.plusDays(1)

        val yyyy1 = nextDate.year.toString()
        val mm1 = nextDate.month.value.toString().padStart(2, '0')
        val dd1 = nextDate.dayOfMonth.toString().padStart(2, '0')

        return """
BEGIN:VEVENT
UID:${uidName}-${yyyy}-${mm}-${dd}
SUMMARY:${name}
DTSTART;VALUE=DATE:${yyyy}${mm}${dd}
DTEND;VALUE=DATE:${yyyy1}${mm1}${dd1}
END:VEVENT
            """.trim()
    }
}