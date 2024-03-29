package co.notix.data

import android.os.Build
import co.notix.callback.NotixCallbackReporter
import co.notix.R
import co.notix.di.contextprovider.ContextProvider
import co.notix.callback.NotixCallback
import co.notix.callback.NotixCallbackStatus
import co.notix.log.Logger
import co.notix.push.permissions.NotificationsPermissionController
import co.notix.utils.getPackageInfoCompat
import co.notix.utils.getTargetSdkVersion
import org.json.JSONArray
import org.json.JSONObject

internal interface MetricsRepository {
    suspend fun sendGeneralMetrics()
    suspend fun sendAppInstall()
    suspend fun sendCanPostNotifications()
}

internal class MetricsRepositoryImpl(
    private val storage: Storage,
    private val contextProvider: ContextProvider,
    private val notificationsPermissionController: NotificationsPermissionController,
    private val metricsDataSource: MetricsDataSource,
    private val notixCallbackReporter: NotixCallbackReporter
) : MetricsRepository {
    override suspend fun sendGeneralMetrics() {
        runCatching {
            val context = contextProvider.appContext
            val createdDate = storage.getCreatedDate()
            val lastRunVersion = storage.getLastRunVersion()
            val currentVersion = context.packageManager
                .getPackageInfoCompat(context.packageName, 0).versionName
            val runCount = storage.getRunCount()
            val appId = storage.getAppId()
            val uuid = storage.getUUID()
            val packageName = context.packageName

            val dataMetric = JSONObject().apply {
                put("version_upgrade", JSONObject().apply {
                    put("previous_version", lastRunVersion)
                    put("current_version", currentVersion)
                })
                put("run_count", JSONObject().apply {
                    put("count", runCount)
                })
                put("client_info", JSONObject().apply {
                    put("android_api", Build.VERSION.SDK_INT)
                    put("app_target_sdk", context.getTargetSdkVersion())
                    put("notix_sdk_version", context.resources.getString(R.string.sdk_version))
                    put("model", Build.MODEL)
                    put("manufacturer", Build.MANUFACTURER)
                    put(
                        "supported_abis",
                        JSONArray().also { arr ->
                            Build.SUPPORTED_ABIS.forEach { abi -> arr.put(abi) }
                        }
                    )
                })
            }

            val metricJson = JSONObject().apply {
                put("metric_type", "general")
                put("created_date", createdDate)
                put("data", dataMetric)
                put("app_id", appId)
                put("uuid", uuid)
                put("package_name", packageName)
            }

            metricsDataSource.sendMetrics(metricJson) to currentVersion
        }.fold(
            onSuccess = { (response, currentVersion) ->
                storage.setLastRunVersion(currentVersion)
                Logger.i("metric general tracked")
                notixCallbackReporter.report(
                    NotixCallback.GeneralMetrics(NotixCallbackStatus.SUCCESS, response)
                )
            },
            onFailure = {
                Logger.e(msg = "couldn't sent metrics ${it.message}", throwable = it)
                notixCallbackReporter.report(
                    NotixCallback.GeneralMetrics(NotixCallbackStatus.FAILURE, it.message)
                )
            }
        )
    }

    override suspend fun sendAppInstall() {
        runCatching {
            val appId = storage.getAppId() ?: error("sendAppInstall - invalid data. appId null")
            val appInstallJson = JSONObject().apply {
                put("metric_type", "app_install")
                put("data", JSONObject().apply {
                    put("app_id", appId)
                })
            }
            metricsDataSource.sendMetrics(appInstallJson)
        }.fold(
            onSuccess = {
                Logger.i("app_install tracked")
                notixCallbackReporter.report(
                    NotixCallback.AppInstall(NotixCallbackStatus.SUCCESS, it)
                )
            },
            onFailure = {
                Logger.e(msg = "couldn't send app_install event ${it.message}", throwable = it)
                notixCallbackReporter.report(
                    NotixCallback.AppInstall(NotixCallbackStatus.FAILURE, it.message)
                )
            }
        )
    }

    override suspend fun sendCanPostNotifications() {
        val canPostNotifications =
            notificationsPermissionController.updateAndGetCanPostNotifications()
        runCatching {
            val canPostNotificationsJson = JSONObject().apply {
                put("metric_type", "permissions")
                put("data", JSONObject().apply {
                    put("can_post_notifications", canPostNotifications)
                })
            }
            metricsDataSource.sendMetrics(canPostNotificationsJson)
            canPostNotifications
        }.fold(
            onSuccess = {
                Logger.i("sent metric can_post_notifications = $canPostNotifications")
            },
            onFailure = {
                Logger.e(msg = "couldn't send can_post_notifications ${it.message}", throwable = it)
            }
        )
    }
}