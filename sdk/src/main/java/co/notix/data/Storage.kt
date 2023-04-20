package co.notix.data

import android.app.Service
import android.content.Context
import android.content.SharedPreferences
import android.text.format.DateFormat
import androidx.core.content.edit
import co.notix.di.SingletonComponent
import co.notix.domain.RequestVars
import java.util.Date
import java.util.UUID

internal class Storage {
    private val context
        get() = SingletonComponent.contextProvider.appContext

    companion object {
        const val NOTIX_PREF_STORAGE = "NOTIX_PREF_STORAGE"

        const val NOTIX_UNIQUE_ID = "NOTIX_PREF_UNIQUE_ID"
        const val NOTIX_CREATED_DATE = "NOTIX_CREATED_DATE"
        const val NOTIX_REMOTE_SENDER_ID = "NOTIX_REMOTE_SENDER_ID"
        const val NOTIX_APP_ID = "NOTIX_APP_ID"
        const val NOTIX_AUTH_TOKEN = "NOTIX_AUTH_TOKEN"
        const val NOTIX_PUB_ID = "NOTIX_PUB_ID"
        const val NOTIX_DEVICE_TOKEN = "NOTIX_DEVICE_TOKEN"
        const val NOTIX_IS_SUBSCRIPTION_ACTIVE = "NOTIX_IS_SUBSCRIPTION_ACTIVE"
        const val NOTIX_INTERSTITIAL_PAYLOAD = "NOTIX_INTERSTITIAL_PAYLOAD"
        const val NOTIX_MESSAGE_CONTENT_DATA = "NOTIX_MESSAGE_CONTENT_DATA"

        const val NOTIX_SUBSCRIBE_REQUEST_VAR_1 = "NOTIX_SUBSCRIBE_REQUEST_VAR_1"
        const val NOTIX_SUBSCRIBE_REQUEST_VAR_2 = "NOTIX_SUBSCRIBE_REQUEST_VAR_2"
        const val NOTIX_SUBSCRIBE_REQUEST_VAR_3 = "NOTIX_SUBSCRIBE_REQUEST_VAR_3"
        const val NOTIX_SUBSCRIBE_REQUEST_VAR_4 = "NOTIX_SUBSCRIBE_REQUEST_VAR_4"
        const val NOTIX_SUBSCRIBE_REQUEST_VAR_5 = "NOTIX_SUBSCRIBE_REQUEST_VAR_5"

        const val NOTIX_INTERSTITIAL_STARTUP_ENABLED = "NOTIX_INTERSTITIAL_STARTUP_ENABLED"
        const val NOTIX_INTERSTITIAL_DEFAULT_ZONE_ID = "NOTIX_INTERSTITIAL_DEFAULT_ZONE_ID"

        const val NOTIX_INTERSTITIAL_REQUEST_VAR_1 = "NOTIX_INTERSTITIAL_REQUEST_VAR_1"
        const val NOTIX_INTERSTITIAL_REQUEST_VAR_2 = "NOTIX_INTERSTITIAL_REQUEST_VAR_2"
        const val NOTIX_INTERSTITIAL_REQUEST_VAR_3 = "NOTIX_INTERSTITIAL_REQUEST_VAR_3"
        const val NOTIX_INTERSTITIAL_REQUEST_VAR_4 = "NOTIX_INTERSTITIAL_REQUEST_VAR_4"
        const val NOTIX_INTERSTITIAL_REQUEST_VAR_5 = "NOTIX_INTERSTITIAL_REQUEST_VAR_5"

        const val NOTIX_INTERSTITIAL_LAST_ZONE_ID = "NOTIX_INTERSTITIAL_ZONE_ID"

        const val NOTIX_LAST_RUN_VERSION = "NOTIX_LAST_RUN_VERSION"
        const val NOTIX_RUN_COUNT = "NOTIX_RUN_COUNT"

        const val NOTIX_CUSTOM_USER_AGENT = "NOTIX_CUSTOM_USER_AGENT"
        const val NOTIX_NOTIFICATION_MODIFIER_CLASS = "NOTIX_NOTIFICATION_MODIFIER_CLASS"
        const val NOTIX_TARGET_EVENT_HANDLER_CLASS = "NOTIX_TARGET_EVENT_HANDLER_CLASS"
    }

    @Synchronized
    fun getUUID(): String {
        var uniqueID = getString(context, NOTIX_UNIQUE_ID)

        if (uniqueID == null) {
            uniqueID = UUID.randomUUID().toString().replace("-", "")
            putString(context, NOTIX_UNIQUE_ID, uniqueID)
        }

        //TODO try make CreatedDate
        getCreatedDate()

        return uniqueID
    }

    @Synchronized
    fun getCreatedDate(): String {
        var createdDate = getString(context, NOTIX_CREATED_DATE)

        if (createdDate == null) {
            createdDate = DateFormat.format("yyyy-MM-dd HH:mm:ss", Date()).toString()
            putString(context, NOTIX_CREATED_DATE, createdDate)
        }

        return createdDate
    }

    fun getSenderId(): Long {
        return getLong(context, NOTIX_REMOTE_SENDER_ID)
    }

    fun setSenderId(senderId: Long) {
        putLong(context, NOTIX_REMOTE_SENDER_ID, senderId)
    }

    fun getLastRunVersion(): String? {
        return getString(context, NOTIX_LAST_RUN_VERSION)
    }

    fun setLastRunVersion(lastRunVersion: String) {
        putString(context, NOTIX_LAST_RUN_VERSION, lastRunVersion)
    }

    fun getRunCount(): Long? {
        return getLong(context, NOTIX_RUN_COUNT)
    }

    fun setRunCount(runCount: Long) {
        putLong(context, NOTIX_RUN_COUNT, runCount)
    }

    fun getPushVars() = RequestVars(
        var1 = getString(context, NOTIX_SUBSCRIBE_REQUEST_VAR_1),
        var2 = getString(context, NOTIX_SUBSCRIBE_REQUEST_VAR_2),
        var3 = getString(context, NOTIX_SUBSCRIBE_REQUEST_VAR_3),
        var4 = getString(context, NOTIX_SUBSCRIBE_REQUEST_VAR_4),
        var5 = getString(context, NOTIX_SUBSCRIBE_REQUEST_VAR_5),
    )

    fun setPushVars(vars: RequestVars? = null) {
        putString(context, NOTIX_SUBSCRIBE_REQUEST_VAR_1, vars?.var1)
        putString(context, NOTIX_SUBSCRIBE_REQUEST_VAR_2, vars?.var2)
        putString(context, NOTIX_SUBSCRIBE_REQUEST_VAR_3, vars?.var3)
        putString(context, NOTIX_SUBSCRIBE_REQUEST_VAR_4, vars?.var4)
        putString(context, NOTIX_SUBSCRIBE_REQUEST_VAR_5, vars?.var5)
    }

    fun getInterstitialVars(context: Context) = RequestVars(
        var1 = getString(context, NOTIX_INTERSTITIAL_REQUEST_VAR_1),
        var2 = getString(context, NOTIX_INTERSTITIAL_REQUEST_VAR_2),
        var3 = getString(context, NOTIX_INTERSTITIAL_REQUEST_VAR_3),
        var4 = getString(context, NOTIX_INTERSTITIAL_REQUEST_VAR_4),
        var5 = getString(context, NOTIX_INTERSTITIAL_REQUEST_VAR_5),
    )

    fun setInterstitialVars(vars: RequestVars? = null) {
        putString(context, NOTIX_INTERSTITIAL_REQUEST_VAR_1, vars?.var1)
        putString(context, NOTIX_INTERSTITIAL_REQUEST_VAR_2, vars?.var2)
        putString(context, NOTIX_INTERSTITIAL_REQUEST_VAR_3, vars?.var3)
        putString(context, NOTIX_INTERSTITIAL_REQUEST_VAR_4, vars?.var4)
        putString(context, NOTIX_INTERSTITIAL_REQUEST_VAR_5, vars?.var5)
    }

    fun getAppId(): String? {
        return getString(context, NOTIX_APP_ID)
    }

    fun setAppId(appId: String) {
        putString(context, NOTIX_APP_ID, appId)
    }

    fun getAuthToken(): String? {
        return getString(context, NOTIX_AUTH_TOKEN)
    }

    fun setAuthToken(authToken: String) {
        putString(context, NOTIX_AUTH_TOKEN, authToken)
    }

    fun getPubId(): Int? {
        return getInt(context, NOTIX_PUB_ID)
    }

    fun setPubId(pubId: Int) {
        putInt(context, NOTIX_PUB_ID, pubId)
    }

    fun getFcmToken(): String? {
        return getString(context, NOTIX_DEVICE_TOKEN)
    }

    fun setFcmToken(appId: String) {
        putString(context, NOTIX_DEVICE_TOKEN, appId)
    }

    fun removeFcmToken() = removeByKey(NOTIX_DEVICE_TOKEN)

    fun isSubscriptionActive() = getBoolean(context, NOTIX_IS_SUBSCRIPTION_ACTIVE)
    fun setSubscriptionActive(newValue: Boolean) =
        putBoolean(context, NOTIX_IS_SUBSCRIPTION_ACTIVE, newValue)

    fun getInterstitialPayload(context: Context): String? {
        return getString(context, NOTIX_INTERSTITIAL_PAYLOAD)
    }

    fun setInterstitialPayload(interstitialPayload: String) {
        putString(context, NOTIX_INTERSTITIAL_PAYLOAD, interstitialPayload)
    }

    fun getMessageContentPayload(): String? {
        return getString(context, NOTIX_MESSAGE_CONTENT_DATA)
    }

    fun setMessageContentPayload(messageContentPayload: String) {
        putString(context, NOTIX_MESSAGE_CONTENT_DATA, messageContentPayload)
    }

    fun getInterstitialLastZoneId(context: Context): Long {
        return getLong(context, NOTIX_INTERSTITIAL_LAST_ZONE_ID)
    }

    fun setInterstitialLastZoneId(context: Context, zoneId: Long) {
        putLong(context, NOTIX_INTERSTITIAL_LAST_ZONE_ID, zoneId)
    }

    fun getInterstitialStartupEnabled(context: Context): Boolean {
        return getBoolean(context, NOTIX_INTERSTITIAL_STARTUP_ENABLED)
    }

    fun setInterstitialStartupEnabled(context: Context, enabled: Boolean) {
        putBoolean(context, NOTIX_INTERSTITIAL_STARTUP_ENABLED, enabled)
    }

    fun getInterstitialDefaultZoneId(context: Context): Long {
        return getLong(context, NOTIX_INTERSTITIAL_DEFAULT_ZONE_ID)
    }

    fun setInterstitialDefaultZoneId(context: Context, zoneId: Long) {
        putLong(context, NOTIX_INTERSTITIAL_DEFAULT_ZONE_ID, zoneId)
    }

    fun getCustomUserAgent(): String? {
        return getString(context, NOTIX_CUSTOM_USER_AGENT)
    }

    fun setCustomUserAgent(context: Context, userAgent: String) {
        putString(context, NOTIX_CUSTOM_USER_AGENT, userAgent)
    }

    fun getNotixNotificationModifierClass() = getString(context, NOTIX_NOTIFICATION_MODIFIER_CLASS)
    fun setNotixNotificationModifierClass(value: String?) =
        putString(context, NOTIX_NOTIFICATION_MODIFIER_CLASS, value)

    fun getNotixTargetEventHandlerClass() = getString(context, NOTIX_TARGET_EVENT_HANDLER_CLASS)
    fun setNotixTargetEventHandlerClass(value: String?) =
        putString(context, NOTIX_TARGET_EVENT_HANDLER_CLASS, value)


    private fun putString(context: Context, key: String, value: String?) {
        val sharedPrefs = context.getSharedPreferences(
            NOTIX_PREF_STORAGE, Service.MODE_PRIVATE
        )

        val editor: SharedPreferences.Editor = sharedPrefs.edit()
        editor.putString(key, value)
        editor.apply()
    }

    private fun getString(context: Context, key: String): String? {
        val sharedPrefs = context.getSharedPreferences(
            NOTIX_PREF_STORAGE, Service.MODE_PRIVATE
        )
        return sharedPrefs.getString(key, null)
    }

    private fun putInt(context: Context, key: String, value: Int) {
        val sharedPrefs = context.getSharedPreferences(
            NOTIX_PREF_STORAGE, Service.MODE_PRIVATE
        )

        val editor: SharedPreferences.Editor = sharedPrefs.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    private fun getInt(context: Context, key: String): Int? {
        val sharedPrefs = context.getSharedPreferences(
            NOTIX_PREF_STORAGE, Service.MODE_PRIVATE
        )
        return sharedPrefs.getInt(key, INT_NULL).let { if (it == INT_NULL) null else it }
    }

    private fun putLong(context: Context, key: String, value: Long) {
        val sharedPrefs = context.getSharedPreferences(
            NOTIX_PREF_STORAGE, Service.MODE_PRIVATE
        )

        val editor: SharedPreferences.Editor = sharedPrefs.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    private fun getLong(context: Context, key: String): Long {
        val sharedPrefs = context.getSharedPreferences(
            NOTIX_PREF_STORAGE, Service.MODE_PRIVATE
        )
        return sharedPrefs.getLong(key, 0)
    }

    private fun putBoolean(context: Context, key: String, value: Boolean) {
        val sharedPrefs = context.getSharedPreferences(
            NOTIX_PREF_STORAGE, Service.MODE_PRIVATE
        )

        val editor: SharedPreferences.Editor = sharedPrefs.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    private fun getBoolean(context: Context, key: String): Boolean {
        val sharedPrefs = context.getSharedPreferences(
            NOTIX_PREF_STORAGE, Service.MODE_PRIVATE
        )
        return sharedPrefs.getBoolean(key, false)
    }

    fun clearInterstitial(context: Context) {
        val sharedPrefs = context.getSharedPreferences(
            NOTIX_PREF_STORAGE, Service.MODE_PRIVATE
        )
        sharedPrefs.edit().remove(NOTIX_INTERSTITIAL_PAYLOAD).apply()
    }

    fun removeByKey(key: String) =
        context.getSharedPreferences(NOTIX_PREF_STORAGE, Context.MODE_PRIVATE).edit { remove(key) }

    fun clearStorage(context: Context) {
        val sharedPrefs = context.getSharedPreferences(
            NOTIX_PREF_STORAGE, Service.MODE_PRIVATE
        )
        sharedPrefs.edit().clear().apply();
    }
}

private const val INT_NULL = Int.MIN_VALUE / 2 + 11