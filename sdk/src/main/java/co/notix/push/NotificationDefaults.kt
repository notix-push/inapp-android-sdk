package co.notix.push

import android.app.Notification
import android.graphics.Color
import co.notix.R

internal val NOTIFICATION_DEFAULT_COLOR = Color.parseColor("#005A2D")
internal const val NOTIFICATION_DEFAULT_DEFAULTS =
    Notification.DEFAULT_VIBRATE or Notification.DEFAULT_SOUND
internal val NOTIFICATION_DEFAULT_LARGE_ICON = R.drawable.ic_notix_notification_large_icon
internal const val NOTIFICATION_DEFAULT_SHOW_BADGE_ICON = true
internal const val NOTIFICATION_DEFAULT_SHOW_ONLY_LAST_NOTIFICATION = false
internal const val NOTIFICATION_DEFAULT_SHOW_TOAST = false
internal val NOTIFICATION_DEFAULT_SMALL_ICON = R.drawable.ic_notix_notification_small_icon
internal val NOTIFICATION_DEFAULT_SOUND = null
internal val NOTIFICATION_DEFAULT_VIBRATION_PATTERN = listOf(500L)