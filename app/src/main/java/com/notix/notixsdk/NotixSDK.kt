package com.notix.notixsdk

import android.content.Context
import android.content.Intent
import android.util.Log
import com.notix.notixsdk.api.ApiClient
import com.notix.notixsdk.domain.DomainModels
import com.notix.notixsdk.domain.NotixSDKInvalidAuthException
import com.notix.notixsdk.domain.NotixSDKNotInitializedException
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

    // public providers
    val audiences: NotixAudienceProvider = NotixAudienceProvider()

    fun init(
        context: Context,
        notixAppId: String,
        notixToken: String,
        config: NotixSDKConfig = NotixSDKConfig()
    ) {
        storage.setAppId(context, notixAppId)
        storage.setAuthToken(context, notixToken)
        storage.setPackageName(context, context.packageName)
        storage.getUUID(context)

        metricService.incrementRunCount(context)
        metricService.doGeneralMetric(context)

        storage.setInterstitialStartupEnabled(context, config.interstitialStartupEnabled)
        storage.setInterstitialDefaultZoneIdd(context, config.interstitialDefaultZoneId)
        storage.setInterstitialVars(context, config.interstitialDefaultVars)

        StartupService().interstitialStartup(context = context)

        hasInitialized = true
    }

    fun enablePushNotifications(
        context: Context,
        vars: DomainModels.RequestVars? = null,
        onFailed: (() -> Unit)? = null,
        onSuccess: ((String) -> Unit)? = null,
    ) {
        val intent = Intent(context, NotificationsPermissionsActivity::class.java)
        context.startActivity(intent)

        if (!hasInitialized) {
            Log.d("NotixDebug", "Notix SDK was not initialized")
            return
        }

        val appId = storage.getAppId(context)
        val authToken = storage.getAuthToken(context)

        if (appId.isNullOrEmpty()) {
            Log.d("NotixDebug", "App id cannot be null or empty")
            return
        }
        if (authToken.isNullOrEmpty()) {
            Log.d("NotixDebug", "Auth token cannot be null or empty")
            return
        }

        val receiveTokenEnrichCallback: (String) -> Unit = { data ->
            apiClient.refresh(context, appId, authToken)
            onSuccess?.invoke(data)
        }

        val initFirebaseProvider = {
            storage.setPackageName(context, context.packageName)
            storage.setAuthToken(context, authToken)
            storage.setPushVars(context, vars)
            storage.getUUID(context)

            notixFirebaseInitProvider = NotixFirebaseInitProvider()
            notixFirebaseInitProvider!!.init(context, receiveTokenEnrichCallback)
        }

        apiClient.getConfig(
            context = context,
            appId = appId,
            authToken = authToken,
            getConfigDoneCallback = initFirebaseProvider,
            onFailed = onFailed,
        )
    }

    fun hasInitialized(): Boolean {
        return hasInitialized
    }
}