package com.notix.notixsdk

import android.app.*
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationCompat

@Suppress("unused")
class NotificationsService {
    fun handleNotification(context: Context, resolver: INotificationActivityResolver, intent: Intent) {
        if (isAppOnForeground(context)) {
            showToast(context, intent)
        }

        val activity = resolver.resolveActivity(intent)
        showNotification(context, activity, intent, NotificationParameters())
    }

    fun handleNotification(context: Context, resolver: INotificationActivityResolver, intent: Intent, notificationParameters: NotificationParameters) {
        if (isAppOnForeground(context)) {
            showToast(context, intent)
        }

        val activity = resolver.resolveActivity(intent)
        showNotification(context, activity, intent, notificationParameters)
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

        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
        }
    }

    private fun showNotification(context: Context, activity: Class<*>, intent: Intent, notificationParameters: NotificationParameters) {
        val title = intent.getStringExtra("title")
        val text = intent.getStringExtra("text")
        val clickData = intent.getStringExtra("click_data")

        val rootIntent = Intent(context, NotificationClickHandlerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val notificationIntent = Intent(context, activity).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        rootIntent.putExtra("notix-notification-intent", notificationIntent)
        rootIntent.putExtra("click_data", clickData)

        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        val pendingIntent = PendingIntent.getActivity(context, 0 , rootIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val channelId = context.getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setDefaults(notificationParameters.defaults)
            .setSmallIcon(notificationParameters.smallIcon)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, notificationParameters.largeIcon))
            .setContentTitle(notificationParameters.title ?: title)
            .setContentText(notificationParameters.text ?: text)
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

            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder)
    }
}