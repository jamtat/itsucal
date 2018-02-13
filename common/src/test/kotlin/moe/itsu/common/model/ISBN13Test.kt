package moe.itsu.common.model

import org.junit.Assert.*
import org.junit.Test

class ISBN13Test {
    @Test
    fun checkISBNScrubbing() {
        val isbn = ISBN13("978-1-947804-02-9")
        assertEquals("9781947804029", isbn.toString())
    }

    @Test
    fun checkSameISBN() {
        val a = ISBN13("978-1-947804-02-9")
        val b = ISBN13("978-1-947804-02-9")
        val c = ISBN13("9781947804029")
        assertEquals(a, b)
        assertEquals(a, c)
        assertEquals(b, c)
        assertEquals(a, a)
    }
}