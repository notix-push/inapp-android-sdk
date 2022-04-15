package com.notix.notixsdk

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.notix.notixsdk.api.ApiClient

class NotixFirebaseInitProvider {
    private var firebaseApp: FirebaseApp? = null
    private val storage = StorageProvider()
    private val apiClient = ApiClient()

    fun init(context: Context, receiveTokenCallback: (String) -> String) {
        val senderId = storage.getSenderId(context)
        if (senderId == 0L) {
            Log.d("NotixDebug", "Fetching sender id failed")
            return
        }
        initFirebaseApp(context, NotixPublicAppId, NotixPublicProjectId, NotixPublicApiKey, senderId ?: NotixPublicSenderId)

        val firebaseMessaging = firebaseApp?.get(FirebaseMessaging::class.java) as FirebaseMessaging
        firebaseMessaging.token.addOnCompleteListener{
            if (it.isSuccessful) {
                val token = it.result
                Log.d("NotixDebug", "Token received $token")

                val availableToken = StorageProvider().getDeviceToken(context)

                if ((availableToken == null && token != null) || availableToken != token) {
                    val appId = StorageProvider().getAppId(context)
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
                    receiveTokenCallback(token)
                }
            } else  {
                Log.d("NotixDebug", "Fetching token failed", it.exception)
            }
        }

        firebaseMessaging.isAutoInitEnabled = true
    }

    private fun initFirebaseApp(context: Context, appId: String, projectId: String, apiKey: String, senderId: Long) {
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
                NotixPublicAppName
            )
        }
    }

    private fun isFirebaseAppAlreadyInitialized(context: Context): Boolean {
        val firebaseApps = FirebaseApp.getApps(context)
        for (app in firebaseApps) {
            if (app.name == NotixPublicAppName) {
                firebaseApp = app
                return true
            }
        }

        return false
    }

    companion object {
        const val NotixPublicAppId: String = "1:105575070626:android:786a2b6f634a6be766d0fb"
        const val NotixPublicProjectId: String = "testforpushespublic"
        const val NotixPublicApiKey: String = "AIzaSyBQfBRsRT3N8jqnzwzOQPGGT9OC0Fn1ea8"

        const val NotixPublicSenderId: Long = 778573163969 // Need get from api server
        const val NotixPublicAppName: String = "NOTIX_SDK_APP_NAME"
    }
}