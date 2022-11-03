package com.notix.notixsdk

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.notix.notixsdk.api.ApiClient
import com.notix.notixsdk.utils.getOrFallback
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL


@Suppress("unused")
class NotificationsService {
    private var apiClient: ApiClient = ApiClient()

    fun handleNotification(context: Context, resolver: INotificationActivityResolver, intent: Intent) {
        val activity = resolver.resolveActivity(intent)
        processNotification(context, activity, intent, NotificationParameters())
    }

    fun handleNotification(context: Context, resolver: INotificationActivityResolver, intent: Intent, notificationParameters: NotificationParameters) {
        if (isAppOnForeground(context) && !hasTargetUrl(intent) && notificationParameters.showToast) {
            showToast(context, intent)
        }

        val activity = resolver.resolveActivity(intent)

        processNotification(context, activity, intent, notificationParameters)
    }

    private fun hasTargetUrl(intent: Intent): Boolean {
        val targetUrlData = intent.getStringExtra("target_url_data")

        return targetUrlData != null && targetUrlData != ""
    }

    private fun isAppOnForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        val packageName = context.packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }

    private fun showToast(context: Context, intent: Intent){
        val text = intent.getStringExtra("text")

        if (text == null || text == "") {
            return
        }

        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
        }
    }

    private fun processNotification(context: Context, activity: Class<*>, intent: Intent, notificationParameters: NotificationParameters) {
        val pingData = intent.getStringExtra("pd")

        val showNotificationThread = Thread {
            try {
                showNotification(context, activity, intent, notificationParameters)
            } catch (e: Exception) {
                Log.d("NotixDebug", "Show notification thread crash: ${e.message?:"[exception message null]"}")
                e.printStackTrace()
            }
        }

        if (pingData.isNullOrEmpty() || JSONObject(pingData).length() == 0) {
            showNotificationThread.start()
            return
        }

        fun callback(response: String) {
            fillMessage(intent, response)
            showNotificationThread.start()
            apiClient.impression(context, intent.getStringExtra("impression_data"))
        }

        apiClient.getPushData(context, pingData, ::callback)
    }

    private fun fillMessage(intent: Intent, response: String) {
        Log.d("NotixDebug", "Message filled: $response")
        val dataJsonArray = JSONArray(response)

        for (i in 0 until dataJsonArray.length()) {
            val item: JSONObject = dataJsonArray.getJSONObject(i)
            intent.putExtra("title", item.getOrFallback("title", ""))
            intent.putExtra("text", item.getOrFallback("description", ""))
            intent.putExtra("click_data", if (item.has("click_data")) item.getString("click_data") else "")
            intent.putExtra("impression_data", if (item.has("impression_data")) item.getString("impression_data") else "")
            intent.putExtra("target_url_data", item.getOrFallback("target_url", ""))
            intent.putExtra("icon_url", item.getOrFallback("icon_url", ""))
            intent.putExtra("image_url", item.getOrFallback("image_url", ""))
        }
    }

    private fun showNotification(context: Context, activity: Class<*>, intent: Intent, notificationParameters: NotificationParameters) {
        var title = intent.getStringExtra("title")
        var text = intent.getStringExtra("text")
        val clickData = intent.getStringExtra("click_data")
        val targetUrlData = intent.getStringExtra("target_url_data")
        val iconUrl = intent.getStringExtra("icon_url")
        val imageUrl = intent.getStringExtra("image_url")

        if (!notificationParameters.title.isNullOrEmpty()) {
            title = notificationParameters.title
        }

        if (!notificationParameters.text.isNullOrEmpty()) {
            text = notificationParameters.text
        }

        val rootIntent : Intent

        val notificationIntent : Intent

        if (!targetUrlData.isNullOrEmpty()) {
            rootIntent = Intent(Intent.ACTION_VIEW)
            rootIntent.data = Uri.parse(targetUrlData)
        } else {
            rootIntent = Intent(context, NotificationClickHandlerActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            notificationIntent = Intent(context, activity).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            rootIntent.putExtra("notix-notification-intent", notificationIntent)
            rootIntent.putExtra("click_data", clickData)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)

        val pendingIntent:PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(context, 0, rootIntent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            // TODO NEED CHECK WORK FLAG_ONE_SHOT
            PendingIntent.getActivity(context, 0, rootIntent, PendingIntent.FLAG_ONE_SHOT)
        }

        val icon: Bitmap? = loadImg(iconUrl)
        val image: Bitmap? = loadImg(imageUrl)

        val channelId = context.getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setDefaults(notificationParameters.defaults)
            .setSmallIcon(notificationParameters.smallIcon)
            .setLargeIcon(icon ?: BitmapFactory.decodeResource(context.resources, notificationParameters.largeIcon))
            .setStyle(NotificationCompat.BigPictureStyle()
                .bigPicture(image)
                .bigLargeIcon(null))
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setVibrate(notificationParameters.vibrationPattern)
            .setSound(notificationParameters.sound ?: defaultSoundUri, AudioManager.STREAM_NOTIFICATION)
            .setPriority(notificationParameters.priority)
            .setColor(notificationParameters.color)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "NOTIX_NOTIFICATION_CHANNEL", NotificationManager.IMPORTANCE_HIGH)
            channel.enableVibration(true)
            channel.vibrationPattern = notificationParameters.vibrationPattern
            if (!notificationParameters.showBadgeIcon) {
                channel.setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder)
    }

    private fun loadImg(imgUrl: String?) : Bitmap? {
        if (imgUrl != null && imgUrl != "") {
            try {
                val url = URL(imgUrl)
                return BitmapFactory.decodeStream(url.openConnection().getInputStream())
            } catch (e: Exception) {
                Log.d(
                    "NotixDebug",
                    "Failed receive image by url: $imgUrl , have ex: ${e.message} "
                )
            }
        }

        return null
    }
}