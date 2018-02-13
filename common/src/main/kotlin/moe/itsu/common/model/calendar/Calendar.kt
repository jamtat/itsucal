package moe.itsu.common.model.calendar

data class Calendar (
    var events: List<CalendarEvent> = ArrayList(),
    val name: String
) {
    override fun toString(): String {
        return """
BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//jamtat//itsucal//EN
CALSCALE:GREGORIAN
METHOD:PUBLISH
X-WR-CALNAME:${name}
${events.map { it.toString() }.joinToString("\n")}
END:VCALENDAR
            """.trim()
    }
}