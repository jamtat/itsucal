package moe.itsu.common.util

private val keyChars = "[0-9A-Za-z_]".toRegex()

fun prepareKeyString(s: String): String =
    s.toLowerCase().filter { it.toString().matches(keyChars) }

private val months = listOf(
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December"
).map { it.toLowerCase() }

fun findMonth(monthString: String): Int = monthString.toLowerCase().let { months.indexOfFirst { m -> m.indexOf(it) == 0 } + 1 }
