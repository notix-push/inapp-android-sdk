package com.notix.notixsdk

import android.app.Notification
import android.graphics.Color
import android.net.Uri
import androidx.core.app.NotificationCompat

class NotificationParameters {
    var defaults: Int = Notification.DEFAULT_VIBRATE or Notification.DEFAULT_SOUND
    var smallIcon: Int = R.drawable.ic_notix_notification_small_icon
    var largeIcon: Int = R.drawable.ic_notix_notification_large_icon
    var color: Int = Color.parseColor("#005A2D")
    var title: String? = null
    var text: String? = null
    var vibrationPattern: LongArray = longArrayOf(500)
    var sound: Uri? = null
    var priority: Int = NotificationCompat.PRIORITY_DEFAULT
}