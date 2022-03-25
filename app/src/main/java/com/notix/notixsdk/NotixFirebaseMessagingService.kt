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

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val event: String = message.data.getOrDefault("event", "main")
        val title: String = message.data.getOrDefault("title", "")
        val text: String = message.data.getOrDefault("text", "")
        val clickData: String = message.data.getOrDefault("click_data", "")
        val impressionData: String = message.data.getOrDefault("impression_data", "")

        Log.d("Debug", "Message received event=$event title=$title text=$text")

        intent = Intent("NOTIX_NOTIFICATION_INTENT")

        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        intent.putExtra("title", title)
        intent.putExtra("text", text)
        intent.putExtra("event", event)
        intent.putExtra("click_data", clickData)
        intent.putExtra("impression_data", impressionData)

        ApiClient().impression(this, impressionData)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val availableToken = StorageProvider().getDeviceToken(this)

        if (availableToken != null && availableToken == token) {
            return
        }

        Log.d("Debug", "New token received $token")

        val appId = StorageProvider().getAppId(this)
        val uuid = StorageProvider().getUUID(this)
        val packageName = this.packageName
        ApiClient().subscribe(this, appId!!, uuid, packageName, token)
    }
}