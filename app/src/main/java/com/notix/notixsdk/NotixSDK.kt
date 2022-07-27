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
        receiveTokenCallback: ((String) -> Unit)? = null,
    ) {
        init(context, notixAppId, notixToken, "", receiveTokenCallback)
    }

    fun init(
        context: Context,
        notixAppId: String,
        notixToken: String,
        requestVar: String,
        receiveTokenCallback: ((String) -> Unit)? = null,
    ) {
        val receiveTokenEnrichCallback: (String) -> Unit = { data ->
            apiClient.refresh(context, notixAppId, notixToken)
            receiveTokenCallback?.invoke(data)
        }

        val initFirebaseProvider = {
            storage.setPackageName(context, context.packageName)
            storage.setAuthToken(context, notixToken)
            storage.setPushRequestVar(context, requestVar)
            storage.getUUID(context)


            notixFirebaseInitProvider = NotixFirebaseInitProvider()
            notixFirebaseInitProvider!!.init(context, receiveTokenEnrichCallback)
        }

        apiClient.getConfig(context, notixAppId, notixToken, initFirebaseProvider)
    }
}