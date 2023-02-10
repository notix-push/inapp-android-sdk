package com.notix.notixsdk

import android.content.Context
import android.util.Log
import com.notix.notixsdk.api.ApiClient
import com.notix.notixsdk.domain.DomainModels
import com.notix.notixsdk.providers.StorageProvider
import com.notix.notixsdk.utils.getOrFallback
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable

data class ContentData(
    val title: String,
    val description: String,
    val imageUrl: String,
    val iconUrl: String,
    val targetUrl: String,
    val event: String,
    val impressionData: String,
    val clickData: String,
) : Serializable

class NotixContentWrapper {
    private val apiClient = ApiClient()
    private val storage = StorageProvider()

    @Volatile
    private var content: ContentData? = null

    @Volatile
    private var isInit = false

    fun load(context: Context, zoneId: Long? = null, vars: DomainModels.RequestVars? = null, onLoadCallback: () -> Unit) {
        val onLoadCallbackForContent = {
            val messageContent = getInterstitialPayload(context)
            if (messageContent != null) {
                isInit = true
                content = messageContent
            }
            onLoadCallback()
        }

        isInit = false
        apiClient.getMessageContent(context, vars, zoneId, onLoadCallbackForContent)
    }

    fun getMessageContent(): ContentData? = content

    fun trackImpression(context: Context) {
        if (!isInit) return
        content?.impressionData?.let { data ->
            apiClient.impression(context, data)
        } ?: Log.d("NotixDebug", "message content impression not tracked")
    }

    /*
    This method do not used in current implementation.
    But it is tied to data that can come and can be used in the future.
     */
    fun trackClick(context: Context) {
        if (!isInit) return
        content?.clickData?.let { data ->
            apiClient.click(context, data)
        } ?: Log.d("NotixDebug", "message content click not tracked")
    }

    private fun getInterstitialPayload(context: Context): ContentData? {
        val messageContentSource = storage.getMessageContentPayload(context)
        val dataJson = JSONArray(messageContentSource)
        val messageContentDtos = parsePayloadJson(dataJson)

        //TODO experiment - first message content data
        return messageContentDtos.firstOrNull()
    }

    private fun parsePayloadJson(dataJson: JSONArray): List<ContentData> {
        val messageContentDtos = mutableListOf<ContentData>()
        for (i in 0 until dataJson.length()) {
            val item: JSONObject = dataJson.getJSONObject(i)

            if (!validateMessagePayloadData(item)) {
                Log.d("NotixDebug", "Message payload item invalid. $item")
                continue
            }

            val messageContentDto = ContentData(
                title = item.getOrFallback("title", ""),
                description = item.getOrFallback("description", ""),
                imageUrl = item.getOrFallback("image_url", ""),
                iconUrl = item.getOrFallback("icon_url", ""),
                targetUrl = item.getOrFallback("target_url", ""),
                event = item.getOrFallback("event", ""),
                if (item.has("impression_data")) item.getString("impression_data") else "",
                if (item.has("click_data")) item.getString("click_data") else "",
            )
            messageContentDtos.add(messageContentDto)
        }
        return messageContentDtos
    }

    private fun validateMessagePayloadData(item: JSONObject): Boolean {
        return item.has("title") && item.has("description") && item.has("icon_url")
    }
}