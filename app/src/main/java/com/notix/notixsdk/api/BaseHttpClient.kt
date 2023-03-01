package com.notix.notixsdk.api

import android.content.Context
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.notix.notixsdk.providers.StorageProvider
import java.util.*

open class BaseHttpClient {
    companion object {
        const val NOTIX_API_BASE_ROUTE = "https://notix.io/api/inapp"
        const val NOTIX_EVENTS_BASE_ROUTE = "https://notix.io/inapp"
        const val NOTIX_BASE_ROUTE = "https://notix.io"
    }

    protected val storage = StorageProvider()

    protected fun getMainHeaders(context: Context): MutableMap<String, String> {
        val headers: MutableMap<String, String> = HashMap()
        headers["Content-Type"] = "application/json"
        headers["Accept-Language"] = Locale.getDefault().toLanguageTag()

        val customUserAgent = storage.getCustomUserAgent(context)

        if (!customUserAgent.isNullOrEmpty()) {
            headers["User-Agent"] = customUserAgent
        } else {
            headers["User-Agent"] = System.getProperty("http.agent")!!
        }

        return headers
    }

    protected fun getRequest(
        context: Context,
        url: String,
        headers: MutableMap<String, String>,
        doResponse: (response: String) -> Unit,
        failResponse: (error: VolleyError) -> Unit,
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
                failResponse(error)
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return headers
            }
        }

        queue.add(stringRequest)
    }

    protected fun postRequest(
        context: Context,
        url: String,
        headers: MutableMap<String, String>,
        body: String,
        doResponse: (response: String) -> Unit,
        failResponse: (error: VolleyError) -> Unit,
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
                failResponse(error)
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