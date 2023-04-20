package co.notix.push.firebase

import co.notix.di.SingletonComponent
import co.notix.log.Logger
import co.notix.push.NOTIFICATION_DEFAULT_COLOR
import co.notix.push.NOTIFICATION_DEFAULT_DEFAULTS
import co.notix.push.NOTIFICATION_DEFAULT_LARGE_ICON
import co.notix.push.NOTIFICATION_DEFAULT_SHOW_BADGE_ICON
import co.notix.push.NOTIFICATION_DEFAULT_SHOW_ONLY_LAST_NOTIFICATION
import co.notix.push.NOTIFICATION_DEFAULT_SHOW_TOAST
import co.notix.push.NOTIFICATION_DEFAULT_SMALL_ICON
import co.notix.push.NOTIFICATION_DEFAULT_SOUND
import co.notix.push.NOTIFICATION_DEFAULT_VIBRATION_PATTERN
import co.notix.push.data.Notification
import co.notix.push.data.NotificationParameters
import co.notix.push.data.NotificationParametersInt
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.launch

internal class NotixFirebaseMessagingService : FirebaseMessagingService() {
    private val contextProviderInitializer = SingletonComponent.contextProviderInitializer
    private val csIo = SingletonComponent.csProvider.provideSupervisedIo()
    private val updateSubscriptionStateUseCase = SingletonComponent.updateSubscriptionStateUseCase
    private val notificationReceiver = SingletonComponent.notificationReceiver

    override fun onCreate() {
        super.onCreate()
        contextProviderInitializer.tryInit(applicationContext)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val notifParams = NotificationParameters(
            color = NOTIFICATION_DEFAULT_COLOR,
            defaults = NOTIFICATION_DEFAULT_DEFAULTS,
            event = message.data["event"],
            iconUrl = message.data["icon_url"],
            imageUrl = message.data["image_url"],
            largeIcon = NOTIFICATION_DEFAULT_LARGE_ICON,
            priority = message.priority,
            showBadgeIcon = NOTIFICATION_DEFAULT_SHOW_BADGE_ICON,
            showOnlyLastNotification = NOTIFICATION_DEFAULT_SHOW_ONLY_LAST_NOTIFICATION,
            showToast = NOTIFICATION_DEFAULT_SHOW_TOAST,
            smallIcon = NOTIFICATION_DEFAULT_SMALL_ICON,
            sound = NOTIFICATION_DEFAULT_SOUND,
            text = message.data["text"],
            title = message.data["title"],
            vibrationPattern = NOTIFICATION_DEFAULT_VIBRATION_PATTERN
        )

        val notifParamsInt = NotificationParametersInt(
            clickData = message.data["click_data"],
            impressionData = message.data["impression_data"],
            targetUrl = message.data["target_url"],
            pingData = message.data["pd"]
        )

        val notification = Notification(notifParams, notifParamsInt)

        Logger.i("Push received $notification")

        csIo.launch {
            notificationReceiver.receive(notification)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        csIo.launch {
            updateSubscriptionStateUseCase()
        }
    }
}