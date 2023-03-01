package com.notix.notixsdk.providers

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.notix.notixsdk.NotixSDK
import com.notix.notixsdk.R
import com.notix.notixsdk.api.ApiClient
import com.notix.notixsdk.domain.*

class NotixFirebaseInitProvider {
    private var firebaseApp: FirebaseApp? = null
    private val storage = StorageProvider()
    private val apiClient = ApiClient()

    fun init(context: Context) {
        val senderId = storage.getSenderId(context)
        if (senderId == 0L) {
            Log.d("NotixDebug", "Fetching sender id failed")
            return
        }
        initFirebaseApp(
            context,
            context.resources.getString(R.string.notix_public_app_id),
            context.resources.getString(R.string.notix_public_project_id),
            context.resources.getString(R.string.notix_public_api_key),
            senderId
        )

        val firebaseMessaging = firebaseApp?.get(FirebaseMessaging::class.java) as FirebaseMessaging
        firebaseMessaging.token.addOnCompleteListener {
            if (it.isSuccessful) {
                val token = it.result
                Log.d("NotixDebug", "Token received $token")

                val availableToken = StorageProvider().getDeviceToken(context)
                val appId = StorageProvider().getAppId(context)
                val authToken = StorageProvider().getAuthToken(context)

                if ((availableToken == null && token != null) || availableToken != token) {

                    val uuid = StorageProvider().getUUID(context)
                    val packageName = StorageProvider().getPackageName(context)
                    if (appId != null && packageName != null) {
                        apiClient.subscribe(context, appId, uuid, packageName, token!!)
                    } else {
                        Log.d(
                            "NotixDebug",
                            "invalid subscribe data (appId: $appId, uuid: $uuid, packageName: $packageName)"
                        )
                    }
                }

                if (token != null) {
                    if (appId != null && authToken != null) {
                        apiClient.refresh(context, appId, authToken)
                    } else {
                        NotixSDK.instance.statusCallback?.invoke(
                            NotixRefreshDataCallback(
                                NotixCallbackStatus.FAILED,
                                "appId and authToken should not be empty"
                            )
                        )
                    }

                    NotixSDK.instance.statusCallback?.invoke(
                        NotixGetTokenCallback(NotixCallbackStatus.SUCCESS, token)
                    )
                }
            } else {
                Log.d("NotixDebug", "Fetching token failed", it.exception)
            }
        }

        firebaseMessaging.isAutoInitEnabled = true
    }

    private fun initFirebaseApp(
        context: Context,
        appId: String,
        projectId: String,
        apiKey: String,
        senderId: Long
    ) {
        val isAlreadyInitialized = isFirebaseAppAlreadyInitialized(context)

        if (!isAlreadyInitialized) {
            val firebaseOptions: FirebaseOptions =
                FirebaseOptions.Builder()
                    .setGcmSenderId(senderId.toString())
                    .setApplicationId(appId)
                    .setApiKey(apiKey)
                    .setProjectId(projectId)
                    .build()

            firebaseApp = FirebaseApp.initializeApp(
                context,
                firebaseOptions,
                context.resources.getString(R.string.notix_public_app_name)
            )
        }
    }

    private fun isFirebaseAppAlreadyInitialized(context: Context): Boolean {
        val firebaseApps = FirebaseApp.getApps(context)
        for (app in firebaseApps) {
            if (app.name == context.resources.getString(R.string.notix_public_app_name)) {
                firebaseApp = app
                return true
            }
        }

        return false
    }
}