package co.notix

import android.app.Activity
import android.content.Context
import co.notix.data.Storage
import co.notix.di.SingletonComponent
import co.notix.domain.RequestVars
import co.notix.interstitial.ClosingSettings
import co.notix.interstitial.InterstitialButton
import co.notix.interstitial.InterstitialContract
import co.notix.interstitial.InterstitialData
import co.notix.interstitial.InterstitialProvider
import co.notix.interstitial.data.AdType
import co.notix.log.Logger
import co.notix.utils.getOrFallback
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

public class NotixInterstitial private constructor() {

    private val interstitialRepository = SingletonComponent.interstitialRepository
    private var provider: InterstitialProvider? = null
    private val storage = Storage()
    private val csIo = SingletonComponent.csProvider.provideSupervisedIo()

    @Volatile
    private var isLoaded = false

    @Volatile
    private var showWaitLoad = false

    private var customButtons: List<InterstitialButton>? = null
    private var closingSettings: ClosingSettings? = null

    private lateinit var clickedCallback: () -> Unit
    private lateinit var dismissedCallback: () -> Unit
    private lateinit var errorCallback: () -> Unit

    private fun init(
        activity: Activity?,
        interstitialClicked: () -> Unit,
        interstitialDismissed: () -> Unit,
        interstitialError: () -> Unit,
        customButtons: List<InterstitialButton>?,
        closingSettings: ClosingSettings?,
    ) {
        if (!SingletonComponent.notixImpl.hasInitialized()) {
            Logger.i("Notix SDK was not initialized")
            return
        }

        clickedCallback = interstitialClicked
        dismissedCallback = interstitialDismissed
        errorCallback = interstitialError

        if (activity != null) {
            clear(activity)
        }
        this.customButtons = customButtons
        this.closingSettings = closingSettings
        this.provider = InterstitialProvider(activity)
    }

    public fun load(
        context: Context,
        zoneId: Long? = null,
        vars: RequestVars? = null,
        experiment: Int? = null,
        onLoadCallback: () -> Unit,
        onLoadFailedCallback: (() -> Unit)? = null,
    ) {
        if (zoneId != null && zoneId > 0) {
            storage.setInterstitialLastZoneId(context, zoneId)
        }

        isLoaded = false

        csIo.launch {
            interstitialRepository.getInterstitial(
                adType = AdType.INTERSTITIAL,
                zoneId = zoneId,
                vars = vars,
                experiment = experiment,
                onLoadCallback = {
                    isLoaded = true
                    if (showWaitLoad) {
                        show(context)
                    }
                    onLoadCallback()
                },
                onLoadFailedCallback = onLoadFailedCallback
            )
        }
    }

    public fun show(context: Context) {
        if (!isLoaded) {
            showWaitLoad = true
            return
        }

        val data = getInterstitialPayload(context)
            ?.run { this@NotixInterstitial.customButtons?.let { this.copy(buttons = it) } ?: this }
            ?.run {
                this@NotixInterstitial.closingSettings?.let { this.copy(closingSettings = it) }
                    ?: this
            }

        if (data != null) {
            Logger.v("data before put: $data")
            provider?.showInterstitial(context, data)
            showWaitLoad = false
        }
    }

    public fun handleResult(requestCode: Int, resultCode: Int) {
        if (requestCode == InterstitialContract.REQUEST_CODE) {
            when (resultCode) {
                InterstitialContract.RESULT_CLICKED -> clickedCallback()
                InterstitialContract.RESULT_DISMISSED -> dismissedCallback()
                InterstitialContract.RESULT_ERROR -> errorCallback()
            }
        }
    }

    private fun clear(context: Context) {
        storage.clearInterstitial(context)
        customButtons = null
        closingSettings = null
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

            if (!validateInterstitialData(item)) {
                Logger.i("Interstitial item invalid. $item")
                continue
            }

            val interstitialDto = InterstitialData(
                title = item.getOrFallback("title", ""),
                description = item.getOrFallback("description", ""),
                imageUrl = item.getOrFallback("image_url", ""),
                iconUrl = item.getOrFallback("icon_url", ""),
                targetUrl = item.getOrFallback("target_url", ""),
                openExternalBrowser = item.getOrFallback("open_external_browser", false),
                // TODO not working for json object string
                //impressionData = item.getOrFallback("impression_data", ""),
                if (item.has("impression_data")) item.getString("impression_data") else "",
                buttons = emptyList(),
                closingSettings = ClosingSettings(timeout = 5, opacity = 1f, sizePercent = 1f),
            )
            interstitialDtos.add(interstitialDto)
        }
        return interstitialDtos
    }

    private fun validateInterstitialData(item: JSONObject): Boolean {
        return item.has("title") && item.has("description") && item.has("image_url")
    }

    public companion object {
        public inline fun build(
            activity: Activity?,
            builder: Builder.() -> Unit,
        ): NotixInterstitial = Builder(activity).apply(builder).build()
    }

    public class Builder(
        private val activity: Activity?
    ) {
        public var interstitialClicked: () -> Unit = {}
        public var interstitialDismissed: () -> Unit = {}
        public var interstitialError: () -> Unit = {}
        public var customButtons: List<InterstitialButton>? = null
        public var closingSettings: ClosingSettings? = null

        public fun build(): NotixInterstitial = NotixInterstitial().apply {
            init(
                activity = activity,
                interstitialClicked = interstitialClicked,
                interstitialDismissed = interstitialDismissed,
                interstitialError = interstitialError,
                customButtons = this@Builder.customButtons,
                closingSettings = this@Builder.closingSettings,
            )
        }
    }
}