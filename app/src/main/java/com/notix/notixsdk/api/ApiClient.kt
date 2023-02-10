package com.notix.notixsdk.api

import android.content.Context
import android.util.Log
import com.notix.notixsdk.domain.DomainModels
import com.notix.notixsdk.R
import com.notix.notixsdk.providers.StorageProvider
import org.json.JSONException
import org.json.JSONObject

class ApiClient : BaseHttpClient() {
    private val storage = StorageProvider()

    fun getConfig(
        context: Context,
        appId: String,
        authToken: String,
        getConfigDoneCallback: () -> Unit,
        onFailed: (() -> Unit)? = null,
    ) {
        val url = "$NOTIX_API_BASE_ROUTE/android/config?app_id=$appId"

        val headers = getMainHeaders()
        headers["Authorization-Token"] = authToken

        getRequest(context, url, headers,
            doResponse = {
                try {
                    val dataJson = JSONObject(it)
                    val configDto = RequestModels.ConfigModel(
                        dataJson.getString("app_id"),
                        dataJson.getInt("pub_id"),
                        dataJson.getLong("sender_id")
                    )

                    if (configDto.appId == "" || configDto.senderId == 0L || configDto.pubId == 0) {
                        Log.d("NotixDebug", "invalid config: $it")
                    } else {
                        storage.setSenderId(context, configDto.senderId)
                        storage.setPubId(context, configDto.pubId)
                        storage.setAppId(context, appId)
                        getConfigDoneCallback()
                    }
                } catch (e: JSONException) {
                    Log.d("NotixDebug", "invalid config json: $it, ${e.message}")
                }
            },
            failResponse = {
                onFailed?.invoke()
            }
        )
    }

    // TODO now it is copy-paste getInterstitial fun
    fun getMessageContent(
        context: Context,
        vars: DomainModels.RequestVars? = null,
        zoneId: Long?,
        getMessageContentDoneCallback: () -> Unit,
    ) {
        val url = "$NOTIX_BASE_ROUTE/interstitial/ewant"

        val headers = getMainHeaders()

        val uuid = StorageProvider().getUUID(context)

        if (uuid.isEmpty()) {
            Log.d("NotixDebug", "uuid is empty")
        }

        val createdDate = StorageProvider().getCreatedDate(context)
        if (createdDate.isEmpty()) {
            Log.d("NotixDebug", "createdDate is empty")
            return
        }

        val appId = StorageProvider().getAppId(context)

        if (appId.isNullOrEmpty()) {
            Log.d("NotixDebug", "app id is empty")
        }

        val pubId = StorageProvider().getPubId(context)

        if (pubId == 0) {
            Log.d("NotixDebug", "pub id is empty")
        }

        try {
            val dataJson = JSONObject().apply {
                put("user", uuid)
                put("app", appId)
                put("pt", 3)
                put("pid", pubId)
                put("cd", createdDate)
                vars?.fillJsonObject(this)
                if (zoneId != null && zoneId > 0) {
                    put("az", zoneId)
                }
            }

            postRequest(context, url, headers, dataJson.toString()) {
                storage.setMessageContentPayload(context, it)

                getMessageContentDoneCallback()
                Log.d("NotixDebug", "message content loaded")
            }
        } catch (e: JSONException) {
            Log.d(
                "NotixDebug",
                "invalid message content request appId: $appId, uuid: $uuid. ${e.message}"
            )
        }
    }

    fun getPushData(context: Context, pd: String, getPushDataCallback: (response: String) -> Unit) {
        val url = "$NOTIX_EVENTS_BASE_ROUTE/ewant"

        val headers = getMainHeaders()

        try {
            postRequest(context, url, headers, pd) {
                getPushDataCallback(it)
            }
        } catch (e: JSONException) {
            Log.d(
                "NotixDebug",
                "invalid pd request pd: $pd. ${e.message}"
            )
        }
    }

    fun getInterstitial(
        context: Context,
        vars: DomainModels.RequestVars? = null,
        zoneId: Long?,
        getInterstitialDoneCallback: () -> Unit
    ) {
        val url = "$NOTIX_BASE_ROUTE/interstitial/ewant"

        val headers = getMainHeaders()

        val uuid = StorageProvider().getUUID(context)

        if (uuid.isEmpty()) {
            Log.d("NotixDebug", "uuid is empty")
        }

        val createdDate = StorageProvider().getCreatedDate(context)
        if (createdDate.isEmpty()) {
            Log.d("NotixDebug", "createdDate is empty")
            return
        }

        val appId = StorageProvider().getAppId(context)

        if (appId.isNullOrEmpty()) {
            Log.d("NotixDebug", "app id is empty")
        }

        val pubId = StorageProvider().getPubId(context)

        if (pubId == 0) {
            Log.d("NotixDebug", "pub id is empty")
        }

        try {
            val dataJson = JSONObject().apply {
                put("user", uuid)
                put("app", appId)
                put("pt", 3)
                put("pid", pubId)
                put("cd", createdDate)
                vars?.fillJsonObject(this)
                if (zoneId != null && zoneId > 0) {
                    put("az", zoneId)
                }
            }

            postRequest(context, url, headers, dataJson.toString()) {
                storage.setInterstitialPayload(context, it)

                getInterstitialDoneCallback()
                Log.d("NotixDebug", "interstitial loaded")
            }
        } catch (e: JSONException) {
            Log.d(
                "NotixDebug",
                "invalid interstitial request appId: $appId, uuid: $uuid. ${e.message}"
            )
        }
    }

    fun subscribe(
        context: Context,
        appId: String,
        uuid: String,
        packageName: String,
        token: String,
    ) {
        val url = "$NOTIX_EVENTS_BASE_ROUTE/android/subscribe"

        val headers = getMainHeaders()

        val createdDate = StorageProvider().getCreatedDate(context)
        if (createdDate.isEmpty()) {
            Log.d("NotixDebug", "createdDate is empty")
        }

        val sdkVersion = context.resources.getString(R.string.sdk_version)
        if (sdkVersion.isEmpty()) {
            Log.d("NotixDebug", "sdkVersion is empty")
        }

        val vars = StorageProvider().getPushVars(context)

        val dataJson = JSONObject().apply {
            put("uuid", uuid)
            put("package_name", packageName)
            put("appId", appId)
            put("token", token)
            put("created_date", createdDate)
            put("sdk_version", sdkVersion)

            if (!vars.isEmpty()) {
                vars.fillJsonObject(this)
            }
        }

        postRequest(context, url, headers, dataJson.toString()) {
            StorageProvider().setDeviceToken(context, token)
        }
    }

    fun impression(context: Context, impressionData: String?) {
        if (impressionData == null) {
            Log.d("NotixDebug", "impression data is empty")
            return
        }

        val url = "$NOTIX_EVENTS_BASE_ROUTE/event"

        val headers = getMainHeaders()

        val appId = StorageProvider().getAppId(context)

        if (appId.isNullOrEmpty()) {
            Log.d("NotixDebug", "app id is empty")
        }
        try {
            val dataJson = JSONObject(impressionData).apply {
                if (!appId.isNullOrEmpty()) {
                    put("app_id", appId)
                }
            }

            postRequest(context, url, headers, dataJson.toString()) {
                Log.d("NotixDebug", "impression tracked")
            }
        } catch (e: JSONException) {
            Log.d("NotixDebug", "invalid impression json: $impressionData, ${e.message}")
        }
    }

    fun click(context: Context, clickData: String?) {
        if (clickData == null) {
            Log.d("NotixDebug", "click data is empty")
            return
        }

        val url = "$NOTIX_EVENTS_BASE_ROUTE/ck"

        val headers = getMainHeaders()

        val appId = StorageProvider().getAppId(context)

        if (appId.isNullOrEmpty()) {
            Log.d("NotixDebug", "app id is empty")
        }

        try {
            val dataJson = JSONObject(clickData).apply {
                if (!appId.isNullOrEmpty()) {
                    put("app_id", appId)
                }
            }

            postRequest(context, url, headers, dataJson.toString()) {
                Log.d("NotixDebug", "click tracked")
            }
        } catch (e: JSONException) {
            Log.d("NotixDebug", "invalid click json: $clickData, ${e.message}")
        }
    }

    fun refresh(context: Context, appId: String, authToken: String) {
        val availableToken = StorageProvider().getDeviceToken(context)
        if (availableToken == null) {
            Log.d("NotixDebug", "refresh canceled, no device token")
            return
        }

        val url = "$NOTIX_API_BASE_ROUTE/refresh?app_id=$appId"

        val headers = getMainHeaders()
        headers["Authorization-Token"] = authToken

        val pubId = StorageProvider().getPubId(context)

        if (pubId == 0) {
            Log.d("NotixDebug", "pub id is empty")
            return
        }

        val uuid = StorageProvider().getUUID(context)

        if (uuid.isEmpty()) {
            Log.d("NotixDebug", "uuid is empty")
            return
        }

        val packageName = StorageProvider().getPackageName(context)

        if (packageName.isNullOrEmpty()) {
            Log.d("NotixDebug", "packageName is empty")
            return
        }

        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val version = pInfo.versionName

        val dataJson = JSONObject().apply {
            put("app_id", appId)
            put("pub_id", pubId)
            put("uuid", uuid)
            put("version", version)
            put("package_name", packageName)
        }

        Log.d("NotixDebug", "refresh: $dataJson")

        postRequest(context, url, headers, dataJson.toString()) {
            Log.d("NotixDebug", "version tracked")
        }
    }

    fun addAudience(context: Context, audience: String) {
        manageAudience(context, "add", audience)
    }

    fun deleteAudience(context: Context, audience: String) {
        manageAudience(context, "delete", audience)
    }

    private fun manageAudience(context: Context, action: String, audience: String) {
        if (action.isEmpty()) {
            Log.d("NotixDebug", "action is empty")
            return
        }

        if (audience.isEmpty()) {
            Log.d("NotixDebug", "audience is empty")
            return
        }

        val authToken = StorageProvider().getAuthToken(context)
        if (authToken.isNullOrEmpty()) {
            Log.d("NotixDebug", "authToken is empty")
            return
        }

        val appId = StorageProvider().getAppId(context)
        if (appId.isNullOrEmpty()) {
            Log.d("NotixDebug", "app id is empty")
            return
        }

        val uuid = StorageProvider().getUUID(context)
        if (uuid.isEmpty()) {
            Log.d("NotixDebug", "uuid is empty")
            return
        }

        val pubId = StorageProvider().getPubId(context)
        if (pubId == 0) {
            Log.d("NotixDebug", "pub id is empty")
            return
        }

        val packageName = StorageProvider().getPackageName(context)

        if (packageName.isNullOrEmpty()) {
            Log.d("NotixDebug", "packageName is empty")
            return
        }

        val url = "$NOTIX_API_BASE_ROUTE/android/audiences?app_id=$appId"

        val headers = getMainHeaders()
        headers["Authorization-Token"] = authToken

        val dataJson = JSONObject().apply {
            put("action", action)
            put("uuid", uuid)
            put("app_id", appId)
            put("pub_id", pubId)
            put("audience", audience)
            put("package_name", packageName)
        }

        postRequest(context, url, headers, dataJson.toString()) {
            Log.d("NotixDebug", "audience managed")
        }
    }
}