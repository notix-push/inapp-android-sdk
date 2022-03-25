package com.notix.notixsdk

import android.app.Service
import android.content.Context
import android.content.SharedPreferences
import java.util.*

class StorageProvider {
    companion object {
        const val NOTIX_PREF_UNIQUE_ID = "NOTIX_PREF_UNIQUE_ID"
        const val NOTIX_REMOTE_SENDER_ID = "NOTIX_REMOTE_SENDER_ID"
        const val NOTIX_APP_ID = "NOTIX_APP_ID"
        const val NOTIX_PUB_ID = "NOTIX_PUB_ID"
        const val NOTIX_DEVICE_TOKEN = "NOTIX_DEVICE_TOKEN"
    }

    @Synchronized
    fun getUUID(context: Context): String {
        var uniqueID = getString(context, NOTIX_PREF_UNIQUE_ID)

        if (uniqueID == null) {
            uniqueID = UUID.randomUUID().toString()
            putString(context, NOTIX_PREF_UNIQUE_ID, uniqueID)
        }

        return uniqueID
    }

    fun getSenderId(context: Context): String? {
        return getString(context, NOTIX_REMOTE_SENDER_ID)
    }

    fun setSenderId(context: Context, senderId: String) {
        putString(context, NOTIX_REMOTE_SENDER_ID, senderId)
    }

    fun getAppId(context: Context): String? {
        return getString(context, NOTIX_APP_ID)
    }

    fun setAppId(context: Context, appId: String) {
        putString(context, NOTIX_APP_ID, appId)
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

    private fun putString(context: Context, key: String, value: String) {
        val sharedPrefs = context.getSharedPreferences(
            key, Service.MODE_PRIVATE
        )

        val editor: SharedPreferences.Editor = sharedPrefs.edit()
        editor.putString(key, value)
        editor.apply()
    }

    private fun getString(context: Context, key: String): String? {
        val sharedPrefs = context.getSharedPreferences(
            key, Service.MODE_PRIVATE
        )
        return sharedPrefs.getString(key, null)
    }

    private fun putInt(context: Context, key: String, value: Int) {
        val sharedPrefs = context.getSharedPreferences(
            key, Service.MODE_PRIVATE
        )

        val editor: SharedPreferences.Editor = sharedPrefs.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    private fun getInt(context: Context, key: String): Int {
        val sharedPrefs = context.getSharedPreferences(
            key, Service.MODE_PRIVATE
        )
        return sharedPrefs.getInt(key, 0)
    }
}