package co.notix.push.data

import co.notix.data.NOTIX_EVENTS_BASE_ROUTE
import co.notix.domain.RequestVars
import co.notix.domain.putRequestVars
import co.notix.log.Logger
import co.notix.network.JSON_MEDIA_TYPE
import co.notix.network.await
import co.notix.network.awaitWithRetries
import co.notix.utils.getStringOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

internal interface PushDataSource {
    suspend fun getPushData(pingData: String): PushDataResponse?
    suspend fun subscribe(
        appId: String,
        uuid: String,
        packageName: String,
        token: String,
        createdDate: String,
        sdkVersion: String,
        vars: RequestVars
    ): String?

    suspend fun unsubscribe(
        appId: String,
        uuid: String,
        packageName: String
    ): String?
}

internal class PushDataSourceImpl(
    private val httpClient: OkHttpClient,
) : PushDataSource {
    override suspend fun getPushData(pingData: String) = withContext(Dispatchers.IO) {
        val body = pingData.toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$NOTIX_EVENTS_BASE_ROUTE/ewant")
            .post(body)
            .build()
        val response = httpClient.newCall(request).await()
        // if the response code is 204 - don't show notification
        if (response.code == 204) return@withContext null
        val responseBody = response.body?.string()
        Logger.i("getPushData responseBody = $responseBody")
        JSONArray(responseBody).getJSONObject(0).run {
            PushDataResponse(
                title = getStringOrNull("title"),
                text = getStringOrNull("text"),
                clickData = getStringOrNull("click_data"),
                impressionData = getStringOrNull("impression_data"),
                targetUrlData = getStringOrNull("target_url"), // поле должно называться target_url_data. на бек заведена таска на исправление.
                iconUrl = getStringOrNull("icon_url"),
                imageUrl = getStringOrNull("image_url")
            )
        }
    }

    override suspend fun subscribe(
        appId: String,
        uuid: String,
        packageName: String,
        token: String,
        createdDate: String,
        sdkVersion: String,
        vars: RequestVars
    ) = withContext(Dispatchers.IO) {
        val dataJson = JSONObject().apply {
            put("uuid", uuid)
            put("package_name", packageName)
            put("appId", appId)
            put("token", token)
            put("created_date", createdDate)
            put("sdk_version", sdkVersion)
            putRequestVars(vars)
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
