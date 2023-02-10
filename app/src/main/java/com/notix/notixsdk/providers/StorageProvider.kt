package com.notix.notixsdk.providers

import android.app.Service
import android.content.Context
import android.content.SharedPreferences
import android.text.format.DateFormat
import com.notix.notixsdk.domain.DomainModels
import java.util.*

class StorageProvider {
    companion object {
        const val NOTIX_PREF_STORAGE = "NOTIX_PREF_STORAGE"

        const val NOTIX_UNIQUE_ID = "NOTIX_PREF_UNIQUE_ID"
        const val NOTIX_CREATED_DATE = "NOTIX_CREATED_DATE"
        const val NOTIX_REMOTE_SENDER_ID = "NOTIX_REMOTE_SENDER_ID"
        const val NOTIX_APP_ID = "NOTIX_APP_ID"
        const val NOTIX_AUTH_TOKEN = "NOTIX_AUTH_TOKEN"
        const val NOTIX_PUB_ID = "NOTIX_PUB_ID"
        const val NOTIX_DEVICE_TOKEN = "NOTIX_DEVICE_TOKEN"
        const val NOTIX_PACKAGE_NAME = "NOTIX_PACKAGE_NAME"
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
        const val NOTIX_NOTIFICATIONS_PERMISSIONS_ALLOWED =
            "NOTIX_NOTIFICATIONS_PERMISSIONS_ALLOWED"
    }

    @Synchronized
    fun getUUID(context: Context): String {
        var uniqueID = getString(context, NOTIX_UNIQUE_ID)

        if (uniqueID == null) {
            uniqueID = UUID.randomUUID().toString().replace("-", "")
            putString(context, NOTIX_UNIQUE_ID, uniqueID)
        }

        //TODO try make CreatedDate
        getCreatedDate(context)

        return uniqueID
    }

    @Synchronized
    fun getCreatedDate(context: Context): String {
        var createdDate = getString(context, NOTIX_CREATED_DATE)

        if (createdDate == null) {
            createdDate = DateFormat.format("yyyy-MM-dd HH:mm:ss", Date()).toString()
            putString(context, NOTIX_CREATED_DATE, createdDate)
        }

        return createdDate
    }

    fun getSenderId(context: Context): Long {
        return getLong(context, NOTIX_REMOTE_SENDER_ID)
    }

    fun setSenderId(context: Context, senderId: Long) {
        putLong(context, NOTIX_REMOTE_SENDER_ID, senderId)
    }

    fun getNotificationsPermissionsAllowed(context: Context): Boolean {
        return getBoolean(context, NOTIX_NOTIFICATIONS_PERMISSIONS_ALLOWED)
    }

    fun setNotificationsPermissionsAllowed(context: Context, allowed: Boolean) {
        putBoolean(context, NOTIX_NOTIFICATIONS_PERMISSIONS_ALLOWED, allowed)
    }

    fun getLastRunVersion(context: Context): String? {
        return getString(context, NOTIX_LAST_RUN_VERSION)
    }

    fun setLastRunVersion(context: Context, lastRunVersion: String) {
        putString(context, NOTIX_LAST_RUN_VERSION, lastRunVersion)
    }

    fun getRunCount(context: Context): Long? {
        return getLong(context, NOTIX_RUN_COUNT)
    }

    fun setRunCount(context: Context, runCount: Long) {
        putLong(context, NOTIX_RUN_COUNT, runCount)
    }

    fun getPushVars(context: Context): DomainModels.RequestVars {
        val var1 = getString(context, NOTIX_SUBSCRIBE_REQUEST_VAR_1) ?: ""
        val var2 = getString(context, NOTIX_SUBSCRIBE_REQUEST_VAR_2) ?: ""
        val var3 = getString(context, NOTIX_SUBSCRIBE_REQUEST_VAR_3) ?: ""
        val var4 = getString(context, NOTIX_SUBSCRIBE_REQUEST_VAR_4) ?: ""
        val var5 = getString(context, NOTIX_SUBSCRIBE_REQUEST_VAR_5) ?: ""

        return DomainModels.RequestVars(var1, var2, var3, var4, var5)
    }

    fun setPushVars(context: Context, vars: DomainModels.RequestVars? = null) {
        if (vars == null) {
            return
        }
        if (vars.var1.isNotEmpty()) {
            putString(context, NOTIX_SUBSCRIBE_REQUEST_VAR_1, vars.var1)
        }
        if (vars.var2.isNotEmpty()) {
            putString(context, NOTIX_SUBSCRIBE_REQUEST_VAR_2, vars.var2)
        }
        if (vars.var3.isNotEmpty()) {
            putString(context, NOTIX_SUBSCRIBE_REQUEST_VAR_3, vars.var3)
        }
        if (vars.var4.isNotEmpty()) {
            putString(context, NOTIX_SUBSCRIBE_REQUEST_VAR_4, vars.var4)
        }
        if (vars.var5.isNotEmpty()) {
            putString(context, NOTIX_SUBSCRIBE_REQUEST_VAR_5, vars.var5)
        }
    }

    fun getInterstitialVars(context: Context): DomainModels.RequestVars {
        val var1 = getString(context, NOTIX_INTERSTITIAL_REQUEST_VAR_1) ?: ""
        val var2 = getString(context, NOTIX_INTERSTITIAL_REQUEST_VAR_2) ?: ""
        val var3 = getString(context, NOTIX_INTERSTITIAL_REQUEST_VAR_3) ?: ""
        val var4 = getString(context, NOTIX_INTERSTITIAL_REQUEST_VAR_4) ?: ""
        val var5 = getString(context, NOTIX_INTERSTITIAL_REQUEST_VAR_5) ?: ""

        return DomainModels.RequestVars(var1, var2, var3, var4, var5)
    }

    fun setInterstitialVars(context: Context, vars: DomainModels.RequestVars? = null) {
        if (vars == null) {
            return
        }
        if (vars.var1.isNotEmpty()) {
            putString(context, NOTIX_INTERSTITIAL_REQUEST_VAR_1, vars.var1)
        }
        if (vars.var2.isNotEmpty()) {
            putString(context, NOTIX_INTERSTITIAL_REQUEST_VAR_2, vars.var2)
        }
        if (vars.var3.isNotEmpty()) {
            putString(context, NOTIX_INTERSTITIAL_REQUEST_VAR_3, vars.var3)
        }
        if (vars.var4.isNotEmpty()) {
            putString(context, NOTIX_INTERSTITIAL_REQUEST_VAR_4, vars.var4)
        }
        if (vars.var5.isNotEmpty()) {
            putString(context, NOTIX_INTERSTITIAL_REQUEST_VAR_5, vars.var5)
        }
    }

    fun getAppId(context: Context): String? {
        return getString(context, NOTIX_APP_ID)
    }

    fun setAppId(context: Context, appId: String) {
        putString(context, NOTIX_APP_ID, appId)
    }

    fun getAuthToken(context: Context): String? {
        return getString(context, NOTIX_AUTH_TOKEN)
    }

    fun setAuthToken(context: Context, authToken: String) {
        putString(context, NOTIX_AUTH_TOKEN, authToken)
    }

    fun getPubId(context: Context): Int {
        return getInt(context, NOTIX_PUB_ID)
    }

    fun setPubId(context: Context, pubId: Int) {
        putInt(context, NOTIX_PUB_ID, pubId)
    }

    fun getDeviceToken(context: Context): String? {
        return getString(context, NOTIX_DEVICE_TOKEN)
    }

    fun setDeviceToken(context: Context, appId: String) {
        putString(context, NOTIX_DEVICE_TOKEN, appId)
    }

    fun getPackageName(context: Context): String? {
        return getString(context, NOTIX_PACKAGE_NAME)
    }

    fun setPackageName(context: Context, packageName: String) {
        putString(context, NOTIX_PACKAGE_NAME, packageName)
    }

    fun getInterstitialPayload(context: Context): String? {
        return getString(context, NOTIX_INTERSTITIAL_PAYLOAD)
    }

    fun setInterstitialPayload(context: Context, interstitialPayload: String) {
        putString(context, NOTIX_INTERSTITIAL_PAYLOAD, interstitialPayload)
    }

    fun getMessageContentPayload(context: Context): String? {
        return getString(context, NOTIX_MESSAGE_CONTENT_DATA)
    }

    fun setMessageContentPayload(context: Context, messageContentPayload: String) {
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

    fun setInterstitialDefaultZoneIdd(context: Context, zoneId: Long) {
        putLong(context, NOTIX_INTERSTITIAL_DEFAULT_ZONE_ID, zoneId)
    }

    private fun putString(context: Context, key: String, value: String) {
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

    private fun getInt(context: Context, key: String): Int {
        val sharedPrefs = context.getSharedPreferences(
            NOTIX_PREF_STORAGE, Service.MODE_PRIVATE
        )
        return sharedPrefs.getInt(key, 0)
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

    fun clearStorage(context: Context) {
        val sharedPrefs = context.getSharedPreferences(
            NOTIX_PREF_STORAGE, Service.MODE_PRIVATE
        )
        sharedPrefs.edit().clear().apply();
    }
}