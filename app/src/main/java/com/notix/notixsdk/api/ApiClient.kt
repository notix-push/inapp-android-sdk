package com.notix.notixsdk.api

import android.content.Context
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.notix.notixsdk.StorageProvider
import java.util.*


class ApiClient {
    private val gson = Gson()
    private val storage = StorageProvider()

    companion object {
        const val NOTIX_API_BASE_ROUTE = "https://notix.io/api/inapp/android"
        const val NOTIX_EVENTS_BASE_ROUTE = "https://notix.io/inapp"
    }

    fun getConfig(context: Context, appId: String, token: String) {
        val url = "$NOTIX_API_BASE_ROUTE/config?app_id=$appId"

        val headers: MutableMap<String, String> = HashMap()
        headers["Authorization-Token"] = token
        headers["Content-Type"] = "application/json"

        getRequest(context, url, headers) {
            val configDto = gson.fromJson(it, RequestModels.ConfigModel::class.java)
            storage.setSenderId(context, configDto.senderId)
            storage.setPubId(context, configDto.pubId)
            storage.setAppId(context, appId)
        }
    }

    fun subscribe(context: Context, appId: String, uuid: String, packageName: String, token: String) {
        val url = "$NOTIX_EVENTS_BASE_ROUTE/android/subscribe"

        val headers: MutableMap<String, String> = HashMap()
        headers["Content-Type"] = "application/json"
        headers["Accept-Language"] = Locale.getDefault().toLanguageTag()

        val body: MutableMap<String, String> = HashMap()
        body["uuid"] = uuid
        body["package_name"] = packageName
        body["appId"] = appId
        body["token"] = token

        postRequestMap(context, url, headers, body) {
            StorageProvider().setDeviceToken(context, token)
        }
    }

    fun impression(context: Context, impressionData: String?) {
        if (impressionData == null) {
            Log.d("Debug", "click data is empty")
            return
        }

        val url = "$NOTIX_EVENTS_BASE_ROUTE/event"

        val headers: MutableMap<String, String> = HashMap()
        headers["Content-Type"] = "application/json"
        headers["Accept-Language"] = Locale.getDefault().toLanguageTag()

        val appId = StorageProvider().getAppId(context)

        if (appId == null) {
            Log.d("Debug", "app id is empty")
            return
        }

        val impressionDto = gson.fromJson(impressionData, RequestModels.ImpressionModel::class.java)
        impressionDto.appId = appId

        postRequestString(context, url, headers, gson.toJson(impressionDto).toString()) {
            Log.d("Debug", "impression tracked")
        }
    }

    fun click(context: Context, clickData: String?) {
        if (clickData == null) {
            Log.d("Debug", "click data is empty")
            return
        }

        val url = "$NOTIX_EVENTS_BASE_ROUTE/ck"

        val headers: MutableMap<String, String> = HashMap()
        headers["Content-Type"] = "application/json"
        headers["Accept-Language"] = Locale.getDefault().toLanguageTag()

        val appId = StorageProvider().getAppId(context)

        if (appId == null) {
            Log.d("Debug", "app id is empty")
            return
        }

        val clickDto = gson.fromJson(clickData, RequestModels.ClickModel::class.java)
        clickDto.appId = appId

        postRequestString(context, url, headers, gson.toJson(clickDto).toString()) {
            Log.d("Debug", "click tracked")
        }
    }

    fun trackActivity(context: Context) {
        val url = "$NOTIX_EVENTS_BASE_ROUTE/activity"

        val headers: MutableMap<String, String> = HashMap()
        headers["Content-Type"] = "application/json"
        headers["Accept-Language"] = Locale.getDefault().toLanguageTag()

        val appId = StorageProvider().getAppId(context)

        if (appId == null) {
            Log.d("Debug", "app id is empty")
            return
        }

        val pubId = StorageProvider().getPubId(context)

        if (pubId == 0) {
            Log.d("Debug", "pub id is empty")
            return
        }

        val activityDto = RequestModels.ActivityModel(appId, pubId)

        postRequestString(context, url, headers, gson.toJson(activityDto).toString()) {
            Log.d("Debug", "activity tracked")
        }
    }

    fun trackVersion(context: Context) {
        val url = "$NOTIX_EVENTS_BASE_ROUTE/version"

        val headers: MutableMap<String, String> = HashMap()
        headers["Content-Type"] = "application/json"
        headers["Accept-Language"] = Locale.getDefault().toLanguageTag()

        val appId = StorageProvider().getAppId(context)

        if (appId == null) {
            Log.d("Debug", "app id is empty")
            return
        }

        val pubId = StorageProvider().getPubId(context)

        if (pubId == 0) {
            Log.d("Debug", "pub id is empty")
            return
        }

        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val version = pInfo.versionName

        val versionDto = RequestModels.VersionModel(appId, pubId, version)

        postRequestString(context, url, headers, gson.toJson(versionDto).toString()) {
            Log.d("Debug", "version tracked")
        }
    }

    private fun getRequest(context: Context, url: String, headers: MutableMap<String, String>, doResponse: (response: String) -> Unit) {
        val queue = Volley.newRequestQueue(context)

        val stringRequest: StringRequest = object : StringRequest(
            Method.GET, url,
            Response.Listener { response ->
                doResponse(response)
            },
            Response.ErrorListener { error ->
                Log.d("Debug", "api client error => $error")
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return headers
            }
        }

        queue.add(stringRequest)
    }

    private fun postRequestMap(context: Context,
                            url: String,
                            headers: MutableMap<String, String>,
                            body: MutableMap<String, String>,
                            doResponse: (response: String) -> Unit) {
        return postRequest(context, url, headers, gson.toJson(body).toString().toByteArray(), doResponse )
    }

    private fun postRequestString(context: Context,
                               url: String,
                               headers: MutableMap<String, String>,
                               body: String,
                               doResponse: (response: String) -> Unit) {
        return postRequest(context, url, headers, body.toByteArray(), doResponse )
    }

    private fun postRequest(context: Context,
                            url: String,
                            headers: MutableMap<String, String>,
                            body: ByteArray,
                            doResponse: (response: String) -> Unit) {
        val queue = Volley.newRequestQueue(context)

        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                doResponse(response)
            },
            Response.ErrorListener { error ->
                Log.d("Debug", "api client error => $error")
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
                return body
            }
        }

        queue.add(stringRequest)
    }
}