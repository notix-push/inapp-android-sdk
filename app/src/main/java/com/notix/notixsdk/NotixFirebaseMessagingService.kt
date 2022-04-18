package com.notix.notixsdk

import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.notix.notixsdk.api.ApiClient

open class NotixFirebaseMessagingService: FirebaseMessagingService() {
    lateinit var intent: Intent
    private var apiClient: ApiClient = ApiClient()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val event: String = message.data.getOrDefault("event", "main")
        val title: String = message.data.getOrDefault("title", "")
        val text: String = message.data.getOrDefault("text", "")
        val clickData: String = message.data.getOrDefault("click_data", "")
        val impressionData: String = message.data.getOrDefault("impression_data", "")
        val targetUrlData: String = message.data.getOrDefault("target_url_data", "")
        val imageUrlData: String = message.data.getOrDefault("image_url_data", "")

        Log.d("NotixDebug", "Message received event=$event title=$title text=$text")

        intent = Intent("NOTIX_NOTIFICATION_INTENT")

        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        intent.putExtra("title", title)
        intent.putExtra("text", text)
        intent.putExtra("event", event)
        intent.putExtra("click_data", clickData)
        intent.putExtra("impression_data", impressionData)
        intent.putExtra("target_url_data", targetUrlData)
        intent.putExtra("image_url_data", imageUrlData)

        ApiClient().impression(this, impressionData)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val availableToken = StorageProvider().getDeviceToken(this)

        if (availableToken != null && availableToken == token) {
            return
        }

        Log.d("NotixDebug", "New token received $token")

        val appId = StorageProvider().getAppId(this)
        val uuid = StorageProvider().getUUID(this)
        val packageName = StorageProvider().getPackageName(this)

        if (appId != null && packageName != null) {
            apiClient.subscribe(this, appId, uuid, packageName, token)
        } else {
            Log.d("NotixDebug", "invalid subscribe data (appId: $appId, uuid: $uuid, packageName: $packageName)")
        }
    }
}