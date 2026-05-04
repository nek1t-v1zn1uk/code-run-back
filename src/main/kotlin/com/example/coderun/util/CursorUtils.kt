package com.example.coderun.util

import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue
import java.util.Base64

object CursorUtil {
    @PublishedApi
    internal val mapper = jacksonObjectMapper()

    fun <T> encode(cursor: T): String {
        val json = mapper.writeValueAsString(cursor)
        return Base64.getUrlEncoder().encodeToString(json.toByteArray())
    }

    inline fun <reified T> decode(token: String?): T? {
        if (token.isNullOrBlank()) return null
        return try {
            val json = String(Base64.getUrlDecoder().decode(token))
            mapper.readValue<T>(json)
        } catch (e: Exception) {
            null
        }
    }
}