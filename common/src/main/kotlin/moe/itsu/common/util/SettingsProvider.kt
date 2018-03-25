package moe.itsu.common.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

interface SettingsProvider {
    fun get(settingName: String): String?
    fun set(settingName: String, value: String)
}