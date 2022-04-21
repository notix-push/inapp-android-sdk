package com.notix.notixsdk

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.notix.notixsdk.api.ApiClient

@Suppress("unused")
class NotixSDK: Service() {
    private val binder = LocalBinder()
    private var context: Context? = null
    private var notixAppId: String = ""
    private var notixToken: String = ""
    private var notixFirebaseInitProvider: NotixFirebaseInitProvider? = null
    private val storage = StorageProvider()
    private val apiClient = ApiClient()

    // public providers
    val audiences: NotixAudienceProvider = NotixAudienceProvider()

    fun init(context: Context, notixAppId: String, notixToken: String) {
        this.init(context, notixAppId, notixToken, ::callbackStub)
    }

    fun init(context: Context, notixAppId: String, notixToken: String, receiveTokenCallback: (String) -> String) {
        this.context = context.applicationContext
        this.notixAppId = notixAppId

        val receiveTokenEnrichCallback : (String) -> String = { data ->
            apiClient.refresh(context, notixAppId, notixToken)
            receiveTokenCallback(data)
        }

        val initFirebaseProvider =  {
            storage.setPackageName(context, context.packageName)
            storage.setAuthToken(context, notixToken)
            storage.getUUID(context)


            notixFirebaseInitProvider = NotixFirebaseInitProvider()
            notixFirebaseInitProvider!!.init(context, receiveTokenEnrichCallback)
        }

        apiClient.getConfig(context, notixAppId, notixToken, initFirebaseProvider)
    }

    private fun callbackStub(token: String): String { return token }

    fun getContext(): Context? {
        return context
    }

    // Remove after
    inner class LocalBinder: Binder() {
        fun getService() : NotixSDK = this@NotixSDK
    }

    override fun onBind(p0: Intent?): IBinder {
        return Binder()
    }
}