package co.notix.data

import co.notix.network.JSON_MEDIA_TYPE
import co.notix.network.awaitWithRetries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

internal interface MainDataSource {
    suspend fun getConfig(appId: String, authToken: String): RequestModels.ConfigModel
    suspend fun manageAudience(
        authToken: String,
        action: String,
        audience: String,
        uuid: String,
        appId: String,
        pubId: Int,
        packageName: String
    ): String?
}

internal class MainDataSourceImpl(
    private val httpClient: OkHttpClient
) : MainDataSource {
    override suspend fun getConfig(appId: String, authToken: String) = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(
                NOTIX_API_BASE_ROUTE.toHttpUrl().newBuilder()
                    .addPathSegment("android")
                    .addPathSegment("config")
                    .addQueryParameter("app_id", appId)
                    .build()
            )
            .addHeader("Authorization-Token", authToken)
            .build()
        val response = httpClient.newCall(request).awaitWithRetries().body?.string()
            ?: error("config == null")
        val responseJson = JSONObject(response)
        RequestModels.ConfigModel(
            appId = responseJson.getString("app_id"),
            pubId = responseJson.getInt("pub_id"),
            senderId = responseJson.getLong("sender_id")
        )
    }

    override suspend fun manageAudience(
        authToken: String,
        action: String,
        audience: String,
        uuid: String,
        appId: String,
        pubId: Int,
        packageName: String
    ) = withContext(Dispatchers.IO) {
        val dataJson = JSONObject().apply {
            put("action", action)
            put("uuid", uuid)
            put("app_id", appId)
            put("pub_id", pubId)
            put("audience", audience)
            put("package_name", packageName)
        }

        val request = Request.Builder()
            .url("$NOTIX_API_BASE_ROUTE/android/audiences?app_id=$appId")
            .addHeader("Authorization-Token", authToken)
            .post(dataJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        httpClient.newCall(request).awaitWithRetries().body?.string()
    }
}