package moe.itsu.common.model

import org.junit.Assert.*
import org.junit.Test

class ISBN13Test {
    @Test
    fun checkISBNScrubbing() {
        val isbn = ISBN13("978-1-947804-02-9")
        assertEquals("9781947804029", isbn.toString())
    }
}