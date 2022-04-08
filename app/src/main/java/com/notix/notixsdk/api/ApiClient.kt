package com.notix.notixsdk.api

import android.content.Context
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
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

    fun getConfig(context: Context, appId: String, token: String, receiveConfigCallback: () -> Unit) {
        val url = "$NOTIX_API_BASE_ROUTE/android/config?app_id=$appId"

        val headers: MutableMap<String, String> = HashMap()
        headers["Authorization-Token"] = token
        headers["Content-Type"] = "application/json"

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
                    receiveConfigCallback()
                }
            } catch (e: JSONException) {
                Log.d("NotixDebug", "invalid config json: $it, ${e.message}")
            }
        }
    }

    fun subscribe(context: Context, appId: String, uuid: String, packageName: String, token: String) {
        val url = "$NOTIX_EVENTS_BASE_ROUTE/android/subscribe"

        val headers: MutableMap<String, String> = HashMap()
        headers["Content-Type"] = "application/json"
        headers["Accept-Language"] = Locale.getDefault().toLanguageTag()

        val dataJson = JSONObject()
        dataJson.put("uuid", uuid)
        dataJson.put("package_name", packageName)
        dataJson.put("appId", appId)
        dataJson.put("token", token)

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

        val headers: MutableMap<String, String> = HashMap()
        headers["Content-Type"] = "application/json"
        headers["Accept-Language"] = Locale.getDefault().toLanguageTag()

        val appId = StorageProvider().getAppId(context)

        if (appId == null) {
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
            Log.d("NotixDebug", "invalid imression json: $impressionData, ${e.message}")
        }

    }

    fun click(context: Context, clickData: String?) {
        if (clickData == null) {
            Log.d("NotixDebug", "click data is empty")
            return
        }

        val url = "$NOTIX_EVENTS_BASE_ROUTE/ck"

        val headers: MutableMap<String, String> = HashMap()
        headers["Content-Type"] = "application/json"
        headers["Accept-Language"] = Locale.getDefault().toLanguageTag()

        val appId = StorageProvider().getAppId(context)

        if (appId == null) {
            Log.d("NotixDebug", "app id is empty")
            return
        }

        try {
            val dataJson = JSONObject(clickData)
            dataJson.put("app_id", appId)

            postRequest(context, url, headers, dataJson.toString()) {
                Log.d("NotixDebug", "click tracked")
            }
        } catch (e: JSONException) {
            Log.d("NotixDebug", "invalid imression json: $clickData, ${e.message}")
        }
    }

    fun refresh(context: Context, appId: String, token: String) {
        val url = "$NOTIX_API_BASE_ROUTE/refresh?app_id=$appId"

        val headers: MutableMap<String, String> = HashMap()
        headers["Content-Type"] = "application/json"
        headers["Authorization-Token"] = token
        headers["Accept-Language"] = Locale.getDefault().toLanguageTag()

        val pubId = StorageProvider().getPubId(context)

        if (pubId == 0) {
            Log.d("NotixDebug", "pub id is empty")
            return
        }

        val uuid = StorageProvider().getUUID(context)

        if (uuid == "") {
            Log.d("NotixDebug", "uuid is empty")
            return
        }

        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val version = pInfo.versionName

        val dataJson = JSONObject()
        dataJson.put("app_id", appId)
        dataJson.put("pub_id", pubId)
        dataJson.put("uuid", uuid)
        dataJson.put("version", version)

        Log.d("NotixDebug", "refresh: $dataJson")

        postRequest(context, url, headers, dataJson.toString()) {
            Log.d("NotixDebug", "version tracked")
        }
    }

    private fun getRequest(context: Context, url: String, headers: MutableMap<String, String>, doResponse: (response: String) -> Unit) {
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

    private fun postRequest(context: Context,
                            url: String,
                            headers: MutableMap<String, String>,
                            body: String,
                            doResponse: (response: String) -> Unit) {
        val queue = Volley.newRequestQueue(context)

        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                Log.d("NotixDebug", "(POST) url => $url | response => $response")
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