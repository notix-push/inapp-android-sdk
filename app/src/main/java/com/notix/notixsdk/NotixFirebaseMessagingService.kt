package com.notix.notixsdk

import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.notix.notixsdk.providers.StorageProvider
import com.notix.notixsdk.api.ApiClient
import com.notix.notixsdk.utils.permissionsAllowed
import org.json.JSONObject

open class NotixFirebaseMessagingService : FirebaseMessagingService() {
    lateinit var intent: Intent
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val event: String = getValueFromData(message.data, "event", "main")
        val title: String = getValueFromData(message.data, "title", "")
        val text: String = getValueFromData(message.data, "text", "")
        val clickData: String = getValueFromData(message.data, "click_data", "")
        val impressionData: String = getValueFromData(message.data, "impression_data", "")
        val targetUrlData: String = getValueFromData(message.data, "target_url_data", "")
        val iconUrl: String = getValueFromData(message.data, "icon_url", "")
        val imageUrl: String = getValueFromData(message.data, "image_url", "")
        val pingData: String = getValueFromData(message.data, "pd", "")

        Log.d("NotixDebug", "Message received event=$event title=$title text=$text")

        intent = Intent("NOTIX_NOTIFICATION_INTENT")

        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        intent.putExtra("event", event)
        intent.putExtra("title", title)
        intent.putExtra("text", text)
        intent.putExtra("click_data", clickData)
        intent.putExtra("impression_data", impressionData)
        intent.putExtra("target_url_data", targetUrlData)
        intent.putExtra("icon_url", iconUrl)
        intent.putExtra("image_url", imageUrl)
        intent.putExtra("pd", pingData)

        if (!permissionsAllowed(this)) {
            return
        }

        if (pingData.isEmpty() || JSONObject(pingData).length() == 0) {
            ApiClient().impression(this, impressionData)
        }
    }

    private var apiClient: ApiClient = ApiClient()

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
            Log.d(
                "NotixDebug",
                "invalid subscribe data (appId: $appId, uuid: $uuid, packageName: $packageName)"
            )
        }
    }
}