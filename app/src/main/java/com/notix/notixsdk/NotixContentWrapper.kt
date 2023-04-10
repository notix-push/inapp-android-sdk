package com.notix.notixsdk

import android.content.Context
import com.notix.notixsdk.di.SingletonComponent
import com.notix.notixsdk.domain.DomainModels
import com.notix.notixsdk.interstitial.data.AdType
import com.notix.notixsdk.log.Logger
import com.notix.notixsdk.providers.StorageProvider
import com.notix.notixsdk.utils.getOrFallback
import java.io.Serializable
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

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

// used by client app
class NotixContentWrapper {
    private val interstitialRepository = SingletonComponent.interstitialRepository
    private val eventReporter = SingletonComponent.eventReporter
    private val cs = SingletonComponent.csProvider.provideSupervisedIo()
    private val storage = StorageProvider()

    @Volatile
    private var content: ContentData? = null

    @Volatile
    private var isInit = false

    fun load(
        context: Context,
        zoneId: Long? = null,
        vars: DomainModels.RequestVars? = null,
        experiment: Int? = null,
        onLoadCallback: () -> Unit,
        onLoadFailedCallback: (() -> Unit)? = null,
    ) {
        isInit = false
        cs.launch {
            interstitialRepository.getInterstitial(
                adType = AdType.CONTENT,
                zoneId = zoneId,
                vars = vars,
                experiment = experiment,
                onLoadCallback = {
                    val messageContent = getInterstitialPayload()
                    if (messageContent != null) {
                        isInit = true
                        content = messageContent
                    }
                    onLoadCallback()
                },
                onLoadFailedCallback = onLoadFailedCallback
            )
        }
    }

    fun getMessageContent(): ContentData? = content

    fun trackImpression(context: Context) {
        if (!isInit) return
        content?.impressionData?.let { data ->
            cs.launch {
                eventReporter.reportImpression(data)
            }
        } ?: Logger.i("message content impression not tracked")
    }

    /*
    This method do not used in current implementation.
    But it is tied to data that can come and can be used in the future.
     */
    fun trackClick(context: Context) {
        if (!isInit) return
        content?.clickData?.let { data ->
            cs.launch { eventReporter.reportClick(data) }
        } ?: Logger.i("message content click not tracked")
    }

    private fun getInterstitialPayload(): ContentData? {
        val messageContentSource = storage.getMessageContentPayload()
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
                Logger.i("Message payload item invalid. $item")
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