package com.notix.notixsdk.push.data

import com.notix.notixsdk.data.NOTIX_EVENTS_BASE_ROUTE
import com.notix.notixsdk.domain.DomainModels
import com.notix.notixsdk.network.JSON_MEDIA_TYPE
import com.notix.notixsdk.network.await
import com.notix.notixsdk.network.awaitWithRetries
import com.notix.notixsdk.network.throwIfBadStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

interface PushDataSource {
    suspend fun getPushData(pingData: String): String?
    suspend fun subscribe(
        appId: String,
        uuid: String,
        packageName: String,
        token: String,
        createdDate: String,
        sdkVersion: String,
        vars: DomainModels.RequestVars
    ): String?

    suspend fun unsubscribe(
        appId: String,
        uuid: String,
        packageName: String
    ): String?
}

class PushDataSourceImpl(
    private val httpClient: OkHttpClient,
) : PushDataSource {
    override suspend fun getPushData(pingData: String) = withContext(Dispatchers.IO) {
        val body = pingData.toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$NOTIX_EVENTS_BASE_ROUTE/ewant")
            .post(body)
            .build()
        httpClient.newCall(request).awaitWithRetries().body?.string()
    }

    override suspend fun subscribe(
        appId: String,
        uuid: String,
        packageName: String,
        token: String,
        createdDate: String,
        sdkVersion: String,
        vars: DomainModels.RequestVars
    ) = withContext(Dispatchers.IO) {
        val dataJson = JSONObject().apply {
            put("uuid", uuid)
            put("package_name", packageName)
            put("appId", appId)
            put("token", token)
            put("created_date", createdDate)
            put("sdk_version", sdkVersion)

            if (!vars.isEmpty()) {
                vars.fillJsonObject(this)
            }
        }

        val body = dataJson.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$NOTIX_EVENTS_BASE_ROUTE/android/subscribe")
            .post(body)
            .build()
        httpClient.newCall(request).awaitWithRetries().body?.string()
    }

    override suspend fun unsubscribe(
        appId: String,
        uuid: String,
        packageName: String
    ) = withContext(Dispatchers.IO) {
        val dataJson = JSONObject().apply {
            put("uuid", uuid)
            put("package_name", packageName)
            put("appId", appId)
        }

        val body = dataJson.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$NOTIX_EVENTS_BASE_ROUTE/android/unsubscribe")
            .post(body)
            .build()
        httpClient.newCall(request).awaitWithRetries().body?.string()
    }
}
