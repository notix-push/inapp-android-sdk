package com.notix.notixsdk

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.work.*
import com.notix.notixsdk.api.ApiClient
import com.notix.notixsdk.interstitial.*
import com.notix.notixsdk.utils.getOrFallback
import org.json.JSONArray
import org.json.JSONObject

class NotixInterstitial private constructor() {

    private var provider: InterstitialProvider? = null
    private val apiClient = ApiClient()
    private val storage = StorageProvider()

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

    fun load(
        context: Context,
        zoneId: Long? = null,
        vars: DomainModels.RequestVars? = null,
        onLoadCallback: () -> Unit
    ) {
        if (zoneId != null && zoneId > 0) {
            storage.setInterstitialZoneId(context, zoneId)
        }

        isLoaded = false

        val onLoadCallbackForShow = {
            isLoaded = true
            if (showWaitLoad) {
                show(context)
            }
            onLoadCallback()
        }

        apiClient.getInterstitial(context, vars, zoneId, onLoadCallbackForShow)
    }

    fun show(context: Context) {
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
            Log.i("NotixDebug", "data before put: $data")
            provider?.showInterstitial(context, data)
            showWaitLoad = false
        }
    }

    fun handleResult(requestCode: Int, resultCode: Int) {
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
                Log.d("NotixDebug", "Interstitial item invalid. $item")
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

    companion object {
        inline fun build(
            activity: Activity?,
            builder: Builder.() -> Unit,
        ) = Builder(activity).apply(builder).build()
    }

    class Builder(
        private val activity: Activity?
    ) {
        var interstitialClicked: () -> Unit = {}
        var interstitialDismissed: () -> Unit = {}
        var interstitialError: () -> Unit = {}
        var customButtons: List<InterstitialButton>? = null
        var closingSettings: ClosingSettings? = null
        var isDisabled: Boolean = false
        var defaultZoneId: Long? = null
        var vars: DomainModels.RequestVars? = null

        fun build() = NotixInterstitial().apply {
            if (activity != null && !this@Builder.isDisabled) {
                doStartup(
                    activity,
                    this@Builder.customButtons?.get(0),
                    this@Builder.closingSettings,
                    this@Builder.defaultZoneId,
                    this@Builder.vars,
                )
            }

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

    private fun doStartup(
        activity: Activity,
        customButton: InterstitialButton?,
        closingSettings: ClosingSettings?,
        defaultZoneId: Long?,
        vars: DomainModels.RequestVars?

    ) {
        //TODO Network connected
        val constraints: Constraints = Constraints.Builder()
            .build()

        val data = Data.Builder()

        if (customButton != null) {
            data.putString(InterstitialStartupWorker.NOTIX_WORKER_BUTTON_TEXT, customButton.text)
            data.putString(
                InterstitialStartupWorker.NOTIX_WORKER_BUTTON_TEXT_COLOR,
                customButton.textColor
            )
            data.putString(
                InterstitialStartupWorker.NOTIX_WORKER_BUTTON_BG_COLOR,
                customButton.backgroundColor
            )
        }

        if (closingSettings != null) {
            data.putInt(
                InterstitialStartupWorker.NOTIX_WORKER_CLOSE_TIMEOUT,
                closingSettings.timeout
            )
            data.putFloat(
                InterstitialStartupWorker.NOTIX_WORKER_CLOSE_OPACITY,
                closingSettings.opacity
            )
            data.putFloat(
                InterstitialStartupWorker.NOTIX_WORKER_CLOSE_SIZE,
                closingSettings.sizePercent
            )
        }

        if (defaultZoneId != null) {
            data.putLong(InterstitialStartupWorker.NOTIX_WORKER_ZONE_ID, defaultZoneId)
        }

        if (vars != null) {
            data.putString(InterstitialStartupWorker.NOTIX_WORKER_VAR_1, vars.var1)
            data.putString(InterstitialStartupWorker.NOTIX_WORKER_VAR_2, vars.var2)
            data.putString(InterstitialStartupWorker.NOTIX_WORKER_VAR_3, vars.var3)
            data.putString(InterstitialStartupWorker.NOTIX_WORKER_VAR_4, vars.var4)
            data.putString(InterstitialStartupWorker.NOTIX_WORKER_VAR_5, vars.var5)
        }

        //TODO Check api version + send data
        val interstitialWorkRequest: OneTimeWorkRequest =
            OneTimeWorkRequest.Builder(InterstitialStartupWorker::class.java)
                .setConstraints(constraints)
                .setInputData(data.build())
                .build()


        WorkManager.getInstance(activity)
            .enqueueUniqueWork(
                InterstitialStartupWorker.NOTIX_WORKER_NAME,
                ExistingWorkPolicy.REPLACE,
                interstitialWorkRequest
            )

    }
}