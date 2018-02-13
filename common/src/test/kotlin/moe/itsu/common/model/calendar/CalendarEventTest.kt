package moe.itsu.common.model.calendar

import org.junit.Test

import org.junit.Assert.*
import java.time.LocalDate

class CalendarEventTest {

    @Test
    fun testCalendarEventStringRepresentation() {
        val event1 = CalendarEvent(
            name = "Test Event",
            date = LocalDate.of(2018, 2, 13)
        )
        val result1 = """
BEGIN:VEVENT
UID:TestEvent-2018-02-13
SUMMARY:Test Event
DTSTART;VALUE=DATE:20180213
DTEND;VALUE=DATE:20180214
END:VEVENT
            """.trim()
        assertEquals(result1, event1.toString())
    }
}