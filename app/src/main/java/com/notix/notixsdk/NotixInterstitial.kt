package com.notix.notixsdk

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import com.notix.notixsdk.api.ApiClient
import com.notix.notixsdk.interstitial.InterstitialData
import com.notix.notixsdk.interstitial.InterstitialProvider
import com.notix.notixsdk.interstitial.InterstitialResult
import org.json.JSONArray
import org.json.JSONObject

class NotixInterstitial {

    private var interstitial: InterstitialProvider? = null
    private val apiClient = ApiClient()
    private val storage = StorageProvider()

    @Volatile
    private var isLoaded = false
    @Volatile
    private var showWaitLoad = false

    fun init(
        activity: ComponentActivity,
        interstitialClicked: () -> Unit = {},
        interstitialDismissed: () -> Unit = {},
        interstitialError: () -> Unit = {},
    ) {
        this.interstitial = InterstitialProvider(
            registry = activity.activityResultRegistry,
            callback = { result ->
                when (result) {
                    InterstitialResult.Clicked -> interstitialClicked()
                    InterstitialResult.Dismissed -> interstitialDismissed()
                    InterstitialResult.Error -> interstitialError()
                }
            }
        ).also(activity.lifecycle::addObserver)
    }

    fun load(context: Context, onLoadCallback: () -> Unit) {
        isLoaded = false

        val onLoadCallbackForShow = {
            isLoaded = true
            if (showWaitLoad) {
                show(context)
            }
            onLoadCallback()
        }

        apiClient.getInterstitial(context, onLoadCallbackForShow)
    }

    fun show(context: Context) {
        if (!isLoaded) {
            showWaitLoad = true
            return
        }
        val data = getInterstitialPayload(context)
        if (data != null) {
            interstitial?.showInterstitial(data)
            showWaitLoad = false
        }
    }

    fun clear(context: Context) {
        storage.clearInterstitial(context)
    }

    private fun getInterstitialPayload(context: Context): InterstitialData? {
        val interstitialSource = storage.getInterstitialPayload(context)
        try {
            val dataJson = JSONArray(interstitialSource)
            val interstitialDtos = parsePayloadJson(dataJson)

            //TODO experiment - first interstitial data
            return interstitialDtos.firstOrNull()
        } catch (e: Exception) {
            Log.d("NotixDebug", "Interstitial parse failed (${e.message ?: ""}). Interstitial content - $interstitialSource")
            return null
        }
    }

    private fun parsePayloadJson(dataJson: JSONArray): List<InterstitialData> {
        val interstitialDtos = mutableListOf<InterstitialData>()
        for (i in 0 until dataJson.length()) {
            val item: JSONObject = dataJson.getJSONObject(i)

            if (!validateInterstitialData(item)) {
                Log.d("NotixDebug", "Interstitial item invalid. $item")
                continue
            }

            val interstitialDto = InterstitialData(
                if (item.has("title")) item.getString("title") else "",
                if (item.has("description")) item.getString("description") else "",
                if (item.has("image_url")) item.getString("image_url") else "",
                if (item.has("icon_url")) item.getString("icon_url") else "",
                if (item.has("target_url")) item.getString("target_url") else "",
                if (item.has("open_external_browser")) item.getBoolean("open_external_browser") else false,
                if (item.has("impression_data")) item.getString("impression_data") else ""
            )
            interstitialDtos.add(interstitialDto)
        }
        return interstitialDtos
    }

    private fun validateInterstitialData(item: JSONObject): Boolean {
        return item.has("title") && item.has("description") && item.has("image_url")
    }
}