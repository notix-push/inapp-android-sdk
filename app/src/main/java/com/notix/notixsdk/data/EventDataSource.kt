package com.notix.notixsdk.data

import com.notix.notixsdk.network.JSON_MEDIA_TYPE
import com.notix.notixsdk.network.await
import com.notix.notixsdk.network.throwIfBadStatusCode
import com.notix.notixsdk.providers.StorageProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

interface EventDataSource {
    suspend fun impression(impressionData: String): String?
    suspend fun click(clickData: String): String?
}

class EventDataSourceImpl(
    private val httpClient: OkHttpClient,
    private val storage: StorageProvider,
) : EventDataSource {
    override suspend fun impression(impressionData: String) = withContext(Dispatchers.IO) {
        val body = verifyJsonAndAddAppId(impressionData).toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$NOTIX_EVENTS_BASE_ROUTE/event")
            .post(body)
            .build()
        httpClient.newCall(request).await().throwIfBadStatusCode().body?.string()
    }

    override suspend fun click(clickData: String) = withContext(Dispatchers.IO) {
        val body = verifyJsonAndAddAppId(clickData).toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$NOTIX_EVENTS_BASE_ROUTE/ck")
            .post(body)
            .build()
        httpClient.newCall(request).await().throwIfBadStatusCode().body?.string()
    }

    private fun verifyJsonAndAddAppId(jsonString: String): String {
        val appId = storage.getAppId()
        return if (appId.isNullOrEmpty()) {
            error("app id is empty when sending event")
        } else {
            JSONObject(jsonString).apply { put("app_id", appId) }.toString()
        }
    }
}