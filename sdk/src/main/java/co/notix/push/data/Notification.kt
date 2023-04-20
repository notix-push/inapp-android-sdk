package co.notix.push.data

import android.net.Uri
import co.notix.utils.Mapper
import co.notix.utils.getStringOrNull
import co.notix.utils.toList
import org.json.JSONArray
import org.json.JSONObject

public data class NotificationParameters(
    val color: Int,
    val defaults: Int,
    val event: String?,
    val iconUrl: String?,
    val imageUrl: String?,
    val largeIcon: Int,
    val priority: Int,
    val showBadgeIcon: Boolean,
    val showOnlyLastNotification: Boolean,
    val showToast: Boolean,
    val smallIcon: Int,
    val sound: Uri?,
    val text: String?,
    val title: String?,
    val vibrationPattern: List<Long>,
)

internal data class NotificationParametersInt(
    val clickData: String?,
    val impressionData: String?,
    val pingData: String?,
    val targetUrlData: String?,
)

internal data class Notification(
    val notifParams: NotificationParameters,
    val notifParamsInt: NotificationParametersInt
)

internal val notificationParametersToJsonMapper =
    object : Mapper<NotificationParameters, JSONObject> {
        override fun map(from: NotificationParameters) = JSONObject().apply {
            put(COLOR, from.color)
            put(DEFAULTS, from.defaults)
            put(EVENT, from.event)
            put(ICON_URL, from.iconUrl)
            put(IMAGE_URL, from.imageUrl)
            put(LARGE_ICON, from.largeIcon)
            put(PRIORITY, from.priority)
            put(SHOW_BADGE_ICON, from.showBadgeIcon)
            put(SHOW_ONLY_LAST_NOTIFICATION, from.showOnlyLastNotification)
            put(SHOW_TOAST, from.showToast)
            put(SMALL_ICON, from.smallIcon)
            put(SOUND, from.sound?.toString())
            put(TEXT, from.text)
            put(TITLE, from.title)
            put(
                VIBRATION_PATTERN,
                JSONArray().also { arr ->
                    from.vibrationPattern.forEach { value -> arr.put(value) }
                })
        }
    }

internal val jsonToNotificationParametersMapper =
    object : Mapper<JSONObject, NotificationParameters> {
        override fun map(from: JSONObject) = with(from) {
            NotificationParameters(
                color = getInt(COLOR),
                defaults = getInt(DEFAULTS),
                event = getStringOrNull(EVENT),
                iconUrl = getStringOrNull(ICON_URL),
                imageUrl = getStringOrNull(IMAGE_URL),
                largeIcon = getInt(LARGE_ICON),
                priority = getInt(PRIORITY),
                showBadgeIcon = getBoolean(SHOW_BADGE_ICON),
                showOnlyLastNotification = getBoolean(SHOW_ONLY_LAST_NOTIFICATION),
                showToast = getBoolean(SHOW_TOAST),
                smallIcon = getInt(SMALL_ICON),
                sound = getStringOrNull(SOUND)?.let { runCatching { Uri.parse(it) }.getOrNull() },
                text = getStringOrNull(TEXT),
                title = getStringOrNull(TITLE),
                vibrationPattern = getJSONArray(VIBRATION_PATTERN).toList()
            )
        }
    }

internal val notificationParametersIntToJsonMapper =
    object : Mapper<NotificationParametersInt, JSONObject> {
        override fun map(from: NotificationParametersInt) = JSONObject().apply {
            put(CLICK_DATA, from.clickData)
            put(IMPRESSION_DATA, from.impressionData)
            put(PING_DATA, from.pingData)
            put(TARGET_URL_DATA, from.targetUrlData)
        }
    }

internal val jsonToNotificationParametersIntMapper =
    object : Mapper<JSONObject, NotificationParametersInt> {
        override fun map(from: JSONObject) = with(from) {
            NotificationParametersInt(
                clickData = getStringOrNull(CLICK_DATA),
                impressionData = getStringOrNull(IMPRESSION_DATA),
                pingData = getStringOrNull(PING_DATA),
                targetUrlData = getStringOrNull(TARGET_URL_DATA)
            )
        }
    }

internal val notificationToJsonMapper = object : Mapper<Notification, JSONObject> {
    override fun map(from: Notification) = JSONObject().apply {
        put(NOTIF_PARAMS, notificationParametersToJsonMapper.map(from.notifParams))
        put(NOTIF_PARAMS_INT, notificationParametersIntToJsonMapper.map(from.notifParamsInt))
    }
}

internal val jsonToNotificationMapper = object : Mapper<JSONObject, Notification> {
    override fun map(from: JSONObject) = Notification(
        notifParams = jsonToNotificationParametersMapper
            .map(from.getJSONObject(NOTIF_PARAMS)),
        notifParamsInt = jsonToNotificationParametersIntMapper
            .map(from.getJSONObject(NOTIF_PARAMS_INT))
    )
}

private const val COLOR = "color"
private const val DEFAULTS = "defaults"
private const val EVENT = "event"
private const val ICON_URL = "iconUrl"
private const val IMAGE_URL = "imageUrl"
private const val LARGE_ICON = "largeIcon"
private const val PRIORITY = "priority"
private const val SHOW_BADGE_ICON = "showBadgeIcon"
private const val SHOW_ONLY_LAST_NOTIFICATION = "showOnlyLastNotification"
private const val SHOW_TOAST = "showToast"
private const val SMALL_ICON = "smallIcon"
private const val SOUND = "sound"
private const val TEXT = "text"
private const val TITLE = "title"
private const val VIBRATION_PATTERN = "vibrationPattern"

private const val CLICK_DATA = "clickData"
private const val IMPRESSION_DATA = "impressionData"
private const val PING_DATA = "pingData"
private const val TARGET_URL_DATA = "targetUrlData"

private const val NOTIF_PARAMS = "notifParams"
private const val NOTIF_PARAMS_INT = "notifParamsInt"