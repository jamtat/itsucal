package moe.itsu.common.util

private val keyChars = "[0-9A-Za-z_]".toRegex()

fun prepareKeyString(s: String): String =
    s.toLowerCase().filter { it.toString().matches(keyChars) }