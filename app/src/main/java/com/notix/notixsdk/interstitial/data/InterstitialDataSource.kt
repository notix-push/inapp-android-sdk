package com.notix.notixsdk.interstitial.data

import com.notix.notixsdk.data.NOTIX_BASE_ROUTE
import com.notix.notixsdk.domain.DomainModels
import com.notix.notixsdk.network.JSON_MEDIA_TYPE
import com.notix.notixsdk.network.awaitWithRetries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

interface InterstitialDataSource {
    suspend fun getInterstitial(
        uuid: String,
        appId: String,
        pubId: Int,
        createdDate: String,
        zoneId: Long?,
        vars: DomainModels.RequestVars?,
        experiment: Int?
    ): String
}

class InterstitialDataSourceImpl(
    private val httpClient: OkHttpClient
) : InterstitialDataSource {
    override suspend fun getInterstitial(
        uuid: String,
        appId: String,
        pubId: Int,
        createdDate: String,
        zoneId: Long?,
        vars: DomainModels.RequestVars?,
        experiment: Int?
    ) = withContext(Dispatchers.IO) {
        val dataJson = JSONObject().apply {
            put("user", uuid)
            put("app", appId)
            put("pt", 3)
            put("pid", pubId)
            put("cd", createdDate)
            vars?.fillJsonObject(this)
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