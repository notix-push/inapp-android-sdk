package co.notix.interstitial.data

import co.notix.data.NOTIX_BASE_ROUTE
import co.notix.domain.RequestVars
import co.notix.domain.putRequestVars
import co.notix.network.JSON_MEDIA_TYPE
import co.notix.network.awaitWithRetries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

internal interface InterstitialDataSource {
    suspend fun getInterstitial(
        uuid: String,
        appId: String,
        pubId: Int,
        createdDate: String,
        zoneId: Long?,
        vars: RequestVars?,
        experiment: Int?
    ): String
}

internal class InterstitialDataSourceImpl(
    private val httpClient: OkHttpClient
) : InterstitialDataSource {
    override suspend fun getInterstitial(
        uuid: String,
        appId: String,
        pubId: Int,
        createdDate: String,
        zoneId: Long?,
        vars: RequestVars?,
        experiment: Int?
    ) = withContext(Dispatchers.IO) {
        val dataJson = JSONObject().apply {
            put("user", uuid)
            put("app", appId)
            put("pt", 3)
            put("pid", pubId)
            put("cd", createdDate)
            vars?.let { putRequestVars(vars) }
            if (zoneId != null && zoneId > 0) {
                put("az", zoneId)
            }
            if (experiment != null) {
                put("experiment", experiment)
            }
        }

        val request = Request.Builder()
            .url("$NOTIX_BASE_ROUTE/interstitial/ewant")
            .post(dataJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        httpClient.newCall(request).awaitWithRetries().body?.string()
            ?: error("interstitial null response")
    }
}