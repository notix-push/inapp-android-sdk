package co.notix.push

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
import androidx.core.app.NotificationCompat
import co.notix.R
import co.notix.di.contextprovider.ContextProvider
import co.notix.log.Logger
import co.notix.perseverance.EventReporter
import co.notix.perseverance.command.CommandResult
import co.notix.push.data.Notification
import co.notix.push.data.PushRepository
import java.net.URL
import kotlin.random.Random
import kotlinx.coroutines.withTimeout

internal interface NotificationHandler {
    suspend fun handleNotification(notification: Notification): CommandResult
}

internal class NotificationHandlerImpl(
    private val reporter: EventReporter,
    private val pushRepository: PushRepository,
    private val contextProvider: ContextProvider,
    private val notificationModifierProvider: NotificationModifierProvider,
) : NotificationHandler {
    override suspend fun handleNotification(notification: Notification): CommandResult {
        var notif = notification
        val pingData = notif.notifParamsInt.pingData
        if (pingData != null && pingData.isNotEmpty() && pingData != "{}") {
            pushRepository.getPushData(pingData).fold(
                onSuccess = { response ->
                    if (response == null) return CommandResult.SUCCESS // no need to show notification if backend returned 204
                    notif = Notification(
                        notifParams = notif.notifParams.copy(
                            title = response.title,
                            text = response.text,
                            iconUrl = response.iconUrl,
                            imageUrl = response.imageUrl
                        ),
                        notifParamsInt = notif.notifParamsInt.copy(
                            clickData = response.clickData,
                            impressionData = response.impressionData,
                            targetUrlData = response.targetUrlData,
                        )
                    )
                },
                onFailure = { return CommandResult.RETRY }
            )
        }

        notificationModifierProvider.get()?.let {
            runCatching {
                withTimeout(1000L) {
                    it.modify(context, notif.notifParams)
                }
            }
                .onSuccess { notif = notif.copy(notifParams = it) }
                .onFailure {
                    Logger.e(
                        msg = "notification modifier error ${it.message}",
                        throwable = it
                    )
                }
        }
        notif.notifParamsInt.impressionData?.let { reporter.reportImpression(it) }

        return showNotification(notif)
    }

    private fun showNotification(notification: Notification): CommandResult {
        val notifParams = notification.notifParams
        val notifParamsInt = notification.notifParamsInt
        val clickHandlerIntent = if (notifParamsInt.targetUrlData.isNullOrEmpty()) {
            Intent(context, NotificationClickHandlerActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("click_data", notifParamsInt.clickData)
                putExtra("event", notifParams.event)
            }
        } else {
            val uri = runCatching { Uri.parse(notifParamsInt.targetUrlData) }.getOrNull()
                ?: return CommandResult.FAILURE
            Intent(Intent.ACTION_VIEW).apply { data = uri }
        }

        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(
                context,
                Random.nextInt(Int.MAX_VALUE),
                clickHandlerIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            // TODO NEED CHECK WORK FLAG_ONE_SHOT
            PendingIntent.getActivity(
                context,
                Random.nextInt(Int.MAX_VALUE),
                clickHandlerIntent,
                PendingIntent.FLAG_ONE_SHOT
            )
        }

        val icon: Bitmap? = loadImg(notifParams.iconUrl)
        val image: Bitmap? = loadImg(notifParams.imageUrl)

        val channelId = context.getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setDefaults(notifParams.defaults)
            .setSmallIcon(notifParams.smallIcon)
            .setLargeIcon(
                icon ?: BitmapFactory.decodeResource(
                    context.resources,
                    notifParams.largeIcon
                )
            )
            .setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(image)
                    .bigLargeIcon(null)
            )
            .setContentTitle(notifParams.title)
            .setContentText(notifParams.text)
            .setAutoCancel(true)
            .setVibrate(notifParams.vibrationPattern.toLongArray())
            .setSound(
                notifParams.sound ?: defaultSoundUri,
                AudioManager.STREAM_NOTIFICATION
            )
            .setPriority(notifParams.priority)
            .setColor(notifParams.color)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "NOTIX_NOTIFICATION_CHANNEL",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.enableVibration(true)
            channel.vibrationPattern = notifParams.vibrationPattern.toLongArray()
            if (!notifParams.showBadgeIcon) {
                channel.setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId =
            if (notifParams.showOnlyLastNotification) 0 else Random.nextInt(Int.MAX_VALUE)

        notificationManager.notify(notificationId, notificationBuilder)
        return CommandResult.SUCCESS
    }

    private fun loadImg(imgUrl: String?): Bitmap? {
        if (imgUrl != null && imgUrl != "") {
            try {
                val url = URL(imgUrl)
                return BitmapFactory.decodeStream(url.openConnection().getInputStream())
            } catch (e: Exception) {
                Logger.i("Failed receive image by url: $imgUrl , have ex: ${e.message} ")
            }
        }

        return null
    }

    private val context
        get() = contextProvider.appContext
}