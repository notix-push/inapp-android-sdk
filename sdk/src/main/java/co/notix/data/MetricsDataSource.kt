package co.notix.data

import co.notix.network.JSON_MEDIA_TYPE
import co.notix.network.awaitWithRetries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

internal interface MetricsDataSource {
    suspend fun sendMetrics(metricsJson: JSONObject): String?
}

internal class MetricsDataSourceImpl(
    private val httpClient: OkHttpClient
) : MetricsDataSource {
    override suspend fun sendMetrics(metricsJson: JSONObject) = withContext(Dispatchers.IO) {
        val body = metricsJson.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$NOTIX_EVENTS_BASE_ROUTE/metrics")
            .post(body)
            .build()
        httpClient.newCall(request).awaitWithRetries().body?.string()
    }
}