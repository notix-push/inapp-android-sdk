package com.notix.notixsdk

import android.content.Context
import com.notix.notixsdk.api.ApiClient

class NotixSDK {
    private var notixFirebaseInitProvider: NotixFirebaseInitProvider? = null
    private val storage = StorageProvider()
    private val apiClient = ApiClient()

    // public providers
    val audiences: NotixAudienceProvider = NotixAudienceProvider()

    fun init(
        context: Context,
        notixAppId: String,
        notixToken: String,
        onFailed: (() -> Unit)? = null,
        onSuccess: ((String) -> Unit)? = null,
    ) {
        init(
            context = context,
            notixAppId = notixAppId,
            notixToken = notixToken,
            vars = null,
            onFailed = onFailed,
            onSuccess = onSuccess,
        )
    }

    fun init(
        context: Context,
        notixAppId: String,
        notixToken: String,
        vars: DomainModels.RequestVars? = null,
        onFailed: (() -> Unit)? = null,
        onSuccess: ((String) -> Unit)? = null,
    ) {
        val receiveTokenEnrichCallback: (String) -> Unit = { data ->
            apiClient.refresh(context, notixAppId, notixToken)
            onSuccess?.invoke(data)
        }

        val initFirebaseProvider = {
            storage.setPackageName(context, context.packageName)
            storage.setAuthToken(context, notixToken)
            storage.setPushVars(context, vars)
            storage.getUUID(context)

            notixFirebaseInitProvider = NotixFirebaseInitProvider()
            notixFirebaseInitProvider!!.init(context, receiveTokenEnrichCallback)
        }

        apiClient.getConfig(
            context = context,
            appId = notixAppId,
            authToken = notixToken,
            getConfigDoneCallback = initFirebaseProvider,
            onFailed = onFailed,
        )
    }
}