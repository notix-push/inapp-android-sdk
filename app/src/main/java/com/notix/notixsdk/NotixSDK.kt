package com.notix.notixsdk

import android.content.Context
import android.content.Intent
import android.util.Log
import com.notix.notixsdk.api.ApiClient
import com.notix.notixsdk.domain.DomainModels
import com.notix.notixsdk.domain.NotixCallback
import com.notix.notixsdk.providers.NotixAudienceProvider
import com.notix.notixsdk.providers.NotixFirebaseInitProvider
import com.notix.notixsdk.providers.StorageProvider
import com.notix.notixsdk.services.MetricsService
import com.notix.notixsdk.services.StartupService

class NotixSDK {
    companion object {
        @kotlin.jvm.JvmField
        val instance: NotixSDK = NotixSDK()
    }

    private var hasInitialized = false

    private var notixFirebaseInitProvider: NotixFirebaseInitProvider? = null
    private val storage = StorageProvider()
    private val apiClient = ApiClient()
    private val metricService = MetricsService()

    // public Units
    val audiences: NotixAudienceProvider = NotixAudienceProvider()
    var statusCallback: ((NotixCallback) -> Unit)? = null

    fun init(
        context: Context,
        notixAppId: String,
        notixToken: String,
        config: NotixSDKConfig = NotixSDKConfig(),
        callbacksHandler: ((NotixCallback) -> Unit)? = null,
    ) {
        storage.setAppId(context, notixAppId)
        storage.setAuthToken(context, notixToken)
        storage.setPackageName(context, context.packageName)
        storage.getUUID(context)

        if (!config.customUserAgent.isNullOrEmpty()) {
            storage.setCustomUserAgent(context, config.customUserAgent!!)
        }

        metricService.incrementRunCount(context)
        metricService.doGeneralMetric(context)

        storage.setInterstitialStartupEnabled(context, config.interstitialStartupEnabled)
        storage.setInterstitialDefaultZoneId(context, config.interstitialDefaultZoneId)
        storage.setInterstitialVars(context, config.interstitialDefaultVars)

        StartupService().interstitialStartup(context = context)

        if (callbacksHandler != null) {
            statusCallback = callbacksHandler
        }

        hasInitialized = true
    }

    fun enablePushNotifications(
        context: Context,
        vars: DomainModels.RequestVars? = null,
    ) {
        if (!hasInitialized) {
            Log.d("NotixDebug", "Notix SDK was not initialized")
            return
        }

        storage.setPushVars(context, vars)

        val intent = Intent(context, NotificationsPermissionsActivity::class.java)
        intent.putExtra("app_id", storage.getAppId(context))
        intent.putExtra("auth_token", storage.getAuthToken(context))

        context.startActivity(intent)
    }

    fun hasInitialized(): Boolean {
        return hasInitialized
    }
}