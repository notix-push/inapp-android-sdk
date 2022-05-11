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

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val event: String =  getValueFromData(message.data, "event", "main")
        val title: String = getValueFromData(message.data, "title", "")
        val text: String = getValueFromData(message.data, "text", "")
        val clickData: String = getValueFromData(message.data, "click_data", "")
        val impressionData: String = getValueFromData(message.data, "impression_data", "")
        val targetUrlData: String = getValueFromData(message.data, "target_url_data", "")
        val iconUrl: String = getValueFromData(message.data, "icon_url", "")
        val imageUrl: String = getValueFromData(message.data, "image_url", "")

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
        intent.putExtra("icon_url", iconUrl)
        intent.putExtra("image_url", imageUrl)

        ApiClient().impression(this, impressionData)
    }

    private fun getValueFromData(data: Map<String, String>, key: String, default: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            data.getOrDefault(key, default)
        } else {
            data[key] ?: default;
        }
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