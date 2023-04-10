package com.notix.notixsdk.utils

import org.json.JSONArray
import org.json.JSONObject

internal inline fun <reified T> JSONObject.getOrFallback(name: String, fallback: T): T =
    if (this.has(name)) (this.get(name) as? T) ?: fallback else fallback

internal inline fun <reified T> JSONArray.toList(): List<T> =
    (0 until this.length()).toList().map { this.get(it) as T }