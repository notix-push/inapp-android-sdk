package com.notix.notixsdk.api

import android.content.Context
import android.util.Log
import com.notix.notixsdk.providers.StorageProvider
import org.json.JSONException
import org.json.JSONObject

class MetricsClient : BaseHttpClient() {
    private val storage = StorageProvider()

    fun trackGeneralMetric(context: Context) {
        val url = "${NOTIX_EVENTS_BASE_ROUTE}/metrics"

        val headers = getMainHeaders()

        val createdDate = storage.getCreatedDate(context)

        val lastRunVersion = storage.getLastRunVersion(context)
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val currentVersion = pInfo.versionName

        val runCount = storage.getRunCount(context)

        val permAllowed = storage.getNotificationsPermissionsAllowed(context)

        try {
            val versionUpgradeMetric = JSONObject().apply {
                put("previous_version", lastRunVersion)
                put("current_version", currentVersion)
            }

            val runCountMetric = JSONObject().apply {
                put("count", runCount)
            }

            val dataMetric = JSONObject().apply {
                put("version_upgrade", versionUpgradeMetric)
                put("run_count", runCountMetric)
            }

            val metricJson = JSONObject().apply {
                put("metric_type", "general")
                put("created_date", createdDate)
                put("permissions_allowed", permAllowed)

                put("data", dataMetric)
            }

            postRequest(context, url, headers, metricJson.toString()) {
                storage.setLastRunVersion(context, currentVersion)

                Log.d("NotixDebug", "metric general tracked")
            }
        } catch (e: JSONException) {
            Log.d(
                "NotixDebug",
                "track general metric failed: ${e.message}"
            )
        }
    }
}