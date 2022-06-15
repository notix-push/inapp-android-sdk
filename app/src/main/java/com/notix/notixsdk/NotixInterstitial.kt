package com.notix.notixsdk

import android.content.Context
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
        apiClient.getInterstitial(context, onLoadCallback)
    }

    fun show(context: Context) {
        val data = getInterstitialPayload(context)
        if (data != null) {
            interstitial?.showInterstitial(data)
        }
    }

    private fun getInterstitialPayload(context: Context): InterstitialData? {
        val interstitialSource = storage.getInterstitialPayload(context)
        val dataJson = JSONArray(interstitialSource)
        val interstitialDtos = parsePayloadJson(dataJson)

        //TODO experiment - first interstitial data
        return interstitialDtos.firstOrNull()
    }

    private fun parsePayloadJson(dataJson: JSONArray): List<InterstitialData> {
        val interstitialDtos = mutableListOf<InterstitialData>()
        for (i in 0 until dataJson.length()) {
            val item: JSONObject = dataJson.getJSONObject(i)
            val interstitialDto = InterstitialData(
                item.getString("title"),
                item.getString("description"),
                item.getString("image_url"),
                item.getString("icon_url"),
                item.getString("target_url"),
                item.getBoolean("open_external_browser"),
                item.getString("impression_data")
            )
            interstitialDtos.add(interstitialDto)
        }
        return interstitialDtos
    }
}