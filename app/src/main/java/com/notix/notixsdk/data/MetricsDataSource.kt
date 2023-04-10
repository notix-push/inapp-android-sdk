package com.notix.notixsdk.data

import com.notix.notixsdk.network.JSON_MEDIA_TYPE
import com.notix.notixsdk.network.awaitWithRetries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

interface MetricsDataSource {
    suspend fun sendMetrics(metricsJson: JSONObject): String?
}

class MetricsDataSourceImpl(
    private val httpClient: OkHttpClient
) : MetricsDataSource {
    override suspend fun sendMetrics(metricsJson: JSONObject) = withContext(Dispatchers.IO) {
        val body = metricsJson.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("${NOTIX_EVENTS_BASE_ROUTE}/metrics")
            .post(body)
            .build()
        httpClient.newCall(request).awaitWithRetries().body?.string()
    }
}