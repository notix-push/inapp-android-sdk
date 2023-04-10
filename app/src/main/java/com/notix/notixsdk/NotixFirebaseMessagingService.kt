package com.notix.notixsdk

import android.content.Intent
import android.os.Build
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.notix.notixsdk.di.SingletonComponent
import com.notix.notixsdk.log.Logger
import kotlinx.coroutines.launch
import org.json.JSONObject

open class NotixFirebaseMessagingService : FirebaseMessagingService() {
    lateinit var intent: Intent
    private val notificationsPermissionController =
        SingletonComponent.notificationsPermissionController
    private val reporter = SingletonComponent.eventReporter
    private val contextProviderInitializer = SingletonComponent.contextProviderInitializer
    private val csIo = SingletonComponent.csProvider.provideSupervisedIo()
    private val updateSubscriptionStateUseCase = SingletonComponent.updateSubscriptionStateUseCase

    override fun onCreate() {
        super.onCreate()
        contextProviderInitializer.tryInit(applicationContext)
    }

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

        Logger.i("Message received event=$event title=$title text=$text")

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

        if (!notificationsPermissionController.updateAndGetCanPostNotifications()) {
            return
        }

        if (pingData.isEmpty() || JSONObject(pingData).length() == 0) {
            reporter.reportImpression(impressionData)
        }
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

        csIo.launch {
            updateSubscriptionStateUseCase()
        }
    }
}