package co.notix.utils

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

internal inline fun <reified T> JSONObject.getOrFallback(name: String, fallback: T): T =
    if (this.has(name)) (this.get(name) as? T) ?: fallback else fallback

internal fun JSONObject.getStringOrNull(name: String) = getOrNull { getString(name) }
internal fun JSONObject.getIntOrNull(name: String) = getOrNull { getInt(name) }
internal fun JSONObject.getLongOrNull(name: String) = getOrNull { getLong(name) }

private inline fun <T> JSONObject.getOrNull(getter: () -> T): T? = try {
    getter()
} catch (e: JSONException) {
    null
}

internal inline fun <reified T> JSONArray.toList() = (0 until this.length()).toList().map {
    when (T::class) {
        Long::class -> this.getLong(it) // workaround, because get() gets Int by default and Int can not be cast to Long
        else -> this.get(it)
    } as T
}