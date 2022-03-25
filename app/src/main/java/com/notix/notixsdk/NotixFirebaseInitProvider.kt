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

    fun init(context: Context, receiveTokenCallback: (String) -> String) {
        val senderId = storage.getSenderId(context)
        initFirebaseApp(context, NotixPublicAppId, NotixPublicProjectId, NotixPublicApiKey, senderId ?: NotixPublicSenderId)

        val firebaseMessaging = firebaseApp?.get(FirebaseMessaging::class.java) as FirebaseMessaging
        firebaseMessaging.token.addOnCompleteListener{
            if (!it.isSuccessful) {
                Log.d("Debug", "Fetching token failed", it.exception)
            }

            val token = it.result
            Log.d("Debug", "Token received $token")

            val availableToken = StorageProvider().getDeviceToken(context)

            if ((availableToken == null && token != null) || availableToken != token) {
                val appId = StorageProvider().getAppId(context)
                val uuid = StorageProvider().getUUID(context)
                val packageName = context.packageName
                ApiClient().subscribe(context, appId!!, uuid, packageName, token!!)
            }

            if (token != null) {
                receiveTokenCallback(token)
            }
        }

        firebaseMessaging.isAutoInitEnabled = true
    }

    private fun initFirebaseApp(context: Context, appId: String, projectId: String, apiKey: String, senderId: String) {
        val isAlreadyInitialized = isFirebaseAppAlreadyInitialized(context)

        if (!isAlreadyInitialized) {
            val firebaseOptions: FirebaseOptions =
                FirebaseOptions.Builder()
                    .setGcmSenderId(senderId)
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

        const val NotixPublicSenderId: String = "778573163969" // Need get from api server
        const val NotixPublicAppName: String = "NOTIX_SDK_APP_NAME"
    }
}