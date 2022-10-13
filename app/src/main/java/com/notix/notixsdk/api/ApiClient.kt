package com.notix.notixsdk.api

import android.content.Context
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.notix.notixsdk.R
import com.notix.notixsdk.StorageProvider
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class ApiClient {
    private val storage = StorageProvider()

    companion object {
        const val NOTIX_API_BASE_ROUTE = "https://notix.io/api/inapp"
        const val NOTIX_EVENTS_BASE_ROUTE = "https://notix.io/inapp"
    }

    private fun getMainHeaders(): MutableMap<String, String> {
        val headers: MutableMap<String, String> = HashMap()
        headers["Content-Type"] = "application/json"
        headers["Accept-Language"] = Locale.getDefault().toLanguageTag()
        headers["User-Agent"] = System.getProperty("http.agent")!!

        return headers
    }

    fun getConfig(
        context: Context,
        appId: String,
        authToken: String,
        getConfigDoneCallback: () -> Unit
    ) {
        val url = "$NOTIX_API_BASE_ROUTE/android/config?app_id=$appId"

        val headers = getMainHeaders()
        headers["Authorization-Token"] = authToken

        getRequest(context, url, headers) {
            try {
                val dataJson = JSONObject(it)
                val configDto = RequestModels.ConfigModel(
                    dataJson.getString("app_id"),
                    dataJson.getInt("pub_id"),
                    dataJson.getLong("sender_id")
                )

                if (configDto.appId == "" || configDto.senderId == 0L || configDto.pubId == 0) {
                    Log.d("NotixDebug", "invalid config: $it")
                } else {
                    storage.setSenderId(context, configDto.senderId)
                    storage.setPubId(context, configDto.pubId)
                    storage.setAppId(context, appId)
                    getConfigDoneCallback()
                }
            } catch (e: JSONException) {
                Log.d("NotixDebug", "invalid config json: $it, ${e.message}")
            }
        }
    }

    // TODO now it is copy-paste getInterstitial fun
    fun getMessageContent(context: Context, requestVar: String?, getMessageContentDoneCallback: () -> Unit) {
        val url = "$NOTIX_EVENTS_BASE_ROUTE/ewant"

        val headers = getMainHeaders()

        val uuid = StorageProvider().getUUID(context)

        if (uuid.isEmpty()) {
            Log.d("NotixDebug", "uuid is empty")
            return
        }

        val createdDate = StorageProvider().getCreatedDate(context)
        if (createdDate.isEmpty()) {
            Log.d("NotixDebug", "createdDate is empty")
            return
        }

        val appId = StorageProvider().getAppId(context)

        if (appId.isNullOrEmpty()) {
            Log.d("NotixDebug", "app id is empty")
            return
        }

        val pubId = StorageProvider().getPubId(context)

        if (pubId == 0) {
            Log.d("NotixDebug", "pub id is empty")
            return
        }

        try {
            val dataJson = JSONObject().apply {
                put("user", uuid)
                put("app", appId)
                put("pt", 3)
                put("pid", pubId)
                put("cd", createdDate)
                if (!requestVar.isNullOrEmpty()) {
                    put("rv", requestVar)
                }
            }

            postRequest(context, url, headers, dataJson.toString()) {
                storage.setMessageContentPayload(context, it)

                getMessageContentDoneCallback()
                Log.d("NotixDebug", "message content loaded")
            }
        } catch (e: JSONException) {
            Log.d(
                "NotixDebug",
                "invalid message content request appId: $appId, uuid: $uuid. ${e.message}"
            )
        }
    }

    fun getInterstitial(context: Context, requestVar: String?, getInterstitialDoneCallback: () -> Unit) {
        val url = "$NOTIX_EVENTS_BASE_ROUTE/ewant"

        val headers = getMainHeaders()

        val uuid = StorageProvider().getUUID(context)

        if (uuid.isEmpty()) {
            Log.d("NotixDebug", "uuid is empty")
            return
        }

        val createdDate = StorageProvider().getCreatedDate(context)
        if (createdDate.isEmpty()) {
            Log.d("NotixDebug", "createdDate is empty")
            return
        }

        val appId = StorageProvider().getAppId(context)

        if (appId.isNullOrEmpty()) {
            Log.d("NotixDebug", "app id is empty")
            return
        }

        val pubId = StorageProvider().getPubId(context)

        if (pubId == 0) {
            Log.d("NotixDebug", "pub id is empty")
            return
        }

        try {
            val dataJson = JSONObject().apply {
                put("user", uuid)
                put("app", appId)
                put("pt", 3)
                put("pid", pubId)
                put("cd", createdDate)
                if (!requestVar.isNullOrEmpty()) {
                    put("rv", requestVar)
                }
            }

            postRequest(context, url, headers, dataJson.toString()) {
                storage.setInterstitialPayload(context, it)

                getInterstitialDoneCallback()
                Log.d("NotixDebug", "interstitial loaded")
            }
        } catch (e: JSONException) {
            Log.d(
                "NotixDebug",
                "invalid interstitial request appId: $appId, uuid: $uuid. ${e.message}"
            )
        }
    }

    fun subscribe(
        context: Context,
        appId: String,
        uuid: String,
        packageName: String,
        token: String
    ) {
        val url = "$NOTIX_EVENTS_BASE_ROUTE/android/subscribe"

        val headers = getMainHeaders()

        val createdDate = StorageProvider().getCreatedDate(context)
        if (createdDate.isEmpty()) {
            Log.d("NotixDebug", "createdDate is empty")
        }

        val sdkVersion = context.resources.getString(R.string.sdk_version)
        if (sdkVersion.isEmpty()) {
            Log.d("NotixDebug", "sdkVersion is empty")
        }

        val requestVar = StorageProvider().getPushRequestVar(context)

        val dataJson = JSONObject().apply {
            put("uuid", uuid)
            put("package_name", packageName)
            put("appId", appId)
            put("token", token)
            put("created_date", createdDate)
            put("sdk_version", sdkVersion)

            if (!requestVar.isNullOrEmpty()) {
                put("var", requestVar)
            }
        }

        postRequest(context, url, headers, dataJson.toString()) {
            StorageProvider().setDeviceToken(context, token)
        }
    }

    fun impression(context: Context, impressionData: String?) {
        if (impressionData == null) {
            Log.d("NotixDebug", "impression data is empty")
            return
        }

        val url = "$NOTIX_EVENTS_BASE_ROUTE/event"

        val headers = getMainHeaders()

        val appId = StorageProvider().getAppId(context)

        if (appId.isNullOrEmpty()) {
            Log.d("NotixDebug", "app id is empty")
            return
        }
        try {
            val dataJson = JSONObject(impressionData)
            dataJson.put("app_id", appId)

            postRequest(context, url, headers, dataJson.toString()) {
                Log.d("NotixDebug", "impression tracked")
            }
        } catch (e: JSONException) {
            Log.d("NotixDebug", "invalid impression json: $impressionData, ${e.message}")
        }
    }

    fun click(context: Context, clickData: String?) {
        if (clickData == null) {
            Log.d("NotixDebug", "click data is empty")
            return
        }

        val url = "$NOTIX_EVENTS_BASE_ROUTE/ck"

        val headers = getMainHeaders()

        val appId = StorageProvider().getAppId(context)

        if (appId.isNullOrEmpty()) {
            Log.d("NotixDebug", "app id is empty")
            return
        }

        try {
            val dataJson = JSONObject(clickData).apply {
                put("app_id", appId)
            }

            postRequest(context, url, headers, dataJson.toString()) {
                Log.d("NotixDebug", "click tracked")
            }
        } catch (e: JSONException) {
            Log.d("NotixDebug", "invalid click json: $clickData, ${e.message}")
        }
    }

    fun refresh(context: Context, appId: String, authToken: String) {
        val availableToken = StorageProvider().getDeviceToken(context)
        if (availableToken == null) {
            Log.d("NotixDebug", "refresh canceled, no device token")
            return
        }

        val url = "$NOTIX_API_BASE_ROUTE/refresh?app_id=$appId"

        val headers = getMainHeaders()
        headers["Authorization-Token"] = authToken

        val pubId = StorageProvider().getPubId(context)

        if (pubId == 0) {
            Log.d("NotixDebug", "pub id is empty")
            return
        }

        val uuid = StorageProvider().getUUID(context)

        if (uuid.isEmpty()) {
            Log.d("NotixDebug", "uuid is empty")
            return
        }

        val packageName = StorageProvider().getPackageName(context)

        if (packageName.isNullOrEmpty()) {
            Log.d("NotixDebug", "packageName is empty")
            return
        }

        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val version = pInfo.versionName

        val dataJson = JSONObject().apply {
            put("app_id", appId)
            put("pub_id", pubId)
            put("uuid", uuid)
            put("version", version)
            put("package_name", packageName)
        }

        Log.d("NotixDebug", "refresh: $dataJson")

        postRequest(context, url, headers, dataJson.toString()) {
            Log.d("NotixDebug", "version tracked")
        }
    }

    fun addAudience(context: Context, audience: String) {
        manageAudience(context, "add", audience)
    }

    fun deleteAudience(context: Context, audience: String) {
        manageAudience(context, "delete", audience)
    }

    private fun manageAudience(context: Context, action: String, audience: String) {
        if (action.isEmpty()) {
            Log.d("NotixDebug", "action is empty")
            return
        }

        if (audience.isEmpty()) {
            Log.d("NotixDebug", "audience is empty")
            return
        }

        val authToken = StorageProvider().getAuthToken(context)
        if (authToken.isNullOrEmpty()) {
            Log.d("NotixDebug", "authToken is empty")
            return
        }

        val appId = StorageProvider().getAppId(context)
        if (appId.isNullOrEmpty()) {
            Log.d("NotixDebug", "app id is empty")
            return
        }

        val uuid = StorageProvider().getUUID(context)
        if (uuid.isEmpty()) {
            Log.d("NotixDebug", "uuid is empty")
            return
        }

        val pubId = StorageProvider().getPubId(context)
        if (pubId == 0) {
            Log.d("NotixDebug", "pub id is empty")
            return
        }

        val packageName = StorageProvider().getPackageName(context)

        if (packageName.isNullOrEmpty()) {
            Log.d("NotixDebug", "packageName is empty")
            return
        }

        val url = "$NOTIX_API_BASE_ROUTE/android/audiences?app_id=$appId"

        val headers = getMainHeaders()
        headers["Authorization-Token"] = authToken

        val dataJson = JSONObject().apply {
            put("action", action)
            put("uuid", uuid)
            put("app_id", appId)
            put("pub_id", pubId)
            put("audience", audience)
            put("package_name", packageName)
        }

        postRequest(context, url, headers, dataJson.toString()) {
            Log.d("NotixDebug", "audience managed")
        }
    }

    private fun getRequest(
        context: Context,
        url: String,
        headers: MutableMap<String, String>,
        doResponse: (response: String) -> Unit
    ) {
        val queue = Volley.newRequestQueue(context)

        val stringRequest: StringRequest = object : StringRequest(
            Method.GET, url,
            Response.Listener { response ->
                Log.d("NotixDebug", "(GET) url => $url | response => $response")
                doResponse(response)
            },
            Response.ErrorListener { error ->
                Log.d("NotixDebug", "api client error => $error")
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return headers
            }
        }

        queue.add(stringRequest)
    }

    private fun postRequest(
        context: Context,
        url: String,
        headers: MutableMap<String, String>,
        body: String,
        doResponse: (response: String) -> Unit,

    ) {
        val queue = Volley.newRequestQueue(context)

        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                Log.d("NotixDebug", "(POST) url => $url | response => $response")
                doResponse(response)
            },
            Response.ErrorListener { error ->
                Log.d("NotixDebug", "api client error")
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return headers
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                return body.toByteArray()
            }
        }

        queue.add(stringRequest)
    }
}