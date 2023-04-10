package com.notix.notixsdk

import android.content.Context
import android.content.Intent
import com.notix.notixsdk.di.SingletonComponent
import com.notix.notixsdk.domain.DomainModels
import com.notix.notixsdk.domain.NotixCallback
import com.notix.notixsdk.log.LogLevel
import com.notix.notixsdk.log.Logger
import com.notix.notixsdk.providers.NotixAudienceProvider
import com.notix.notixsdk.push.permissions.NotificationsPermissionActivity
import com.notix.notixsdk.services.MetricsService
import com.notix.notixsdk.services.StartupService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class NotixSDK private constructor() {
    companion object {
        @JvmField
        val instance: NotixSDK = NotixSDK()
    }

    private val initMutex = Mutex()
    private var initialized = false
    private var pushesInitialized = false

    private val storage = SingletonComponent.storage
    private val metricService = MetricsService()
    private val notificationsPermissionController =
        SingletonComponent.notificationsPermissionController
    private val contextProvider = SingletonComponent.contextProvider
    private val workerLauncher = SingletonComponent.sendEventsWorkerLauncher
    private val configRepository = SingletonComponent.mainRepository
    private val csIo = SingletonComponent.csProvider.provideSupervisedIo()
    private val notixCallbackReporter = SingletonComponent.notixCallbackReporter
    private val updateSubscriptionStateUseCase = SingletonComponent.updateSubscriptionStateUseCase
    private val metricsRepository = SingletonComponent.metricsRepository

    // public Units
    val audiences: NotixAudienceProvider = NotixAudienceProvider()

    fun init(
        context: Context,
        notixAppId: String,
        notixToken: String,
        config: NotixSDKConfig = NotixSDKConfig(),
        callbacksHandler: ((NotixCallback) -> Unit)? = null,
    ) {
        csIo.launch {
            initMutex.withLock {
                if (initialized) return@launch Logger.e("Notix SDK is already initialized. Exiting init")
                SingletonComponent.contextProviderInitializer.tryInit(context)

                storage.setAppId(notixAppId)
                storage.setAuthToken(notixToken)
                storage.getUUID()

                if (!config.customUserAgent.isNullOrEmpty()) {
                    storage.setCustomUserAgent(context, config.customUserAgent!!)
                }

                notixCallbackReporter.setHandler(callbacksHandler)

                metricService.onLaunch()

                storage.setInterstitialStartupEnabled(context, config.interstitialStartupEnabled)
                storage.setInterstitialDefaultZoneId(context, config.interstitialDefaultZoneId)
                storage.setInterstitialVars(context, config.interstitialDefaultVars)

                configRepository.getConfig(appId = notixAppId, authToken = notixToken)
                    ?: return@launch

                workerLauncher.launch()
                StartupService().interstitialStartup(context = context)

                initialized = true
            }
        }
    }

    fun enablePushNotifications(
        context: Context,
        vars: DomainModels.RequestVars? = null,
    ) {
        csIo.launch {
            delay(10) // enablePushNotifications is sometimes launched before init. waiting for init to acquire mutex first.
            initMutex.withLock {
                if (!initialized) return@launch Logger.e("Notix SDK was not initialized. Exiting enablePushNotifications")
                if (pushesInitialized) return@launch Logger.e("Push notifications are already initialized. Exiting enablePushNotifications")

                storage.setPushVars(context, vars)

                if (notificationsPermissionController.isPermissionNeeded()) {
                    val act = contextProvider.currentActivity.filterNotNull().first()
                    act.startActivity(Intent(act, NotificationsPermissionActivity::class.java))
                } else {
                    notificationsPermissionController.updateAndGetCanPostNotifications()
                }

                csIo.launch {
                    notificationsPermissionController.canPostNotificationsF.filterNotNull()
                        .collect {
                            launch { metricsRepository.sendCanPostNotifications() }
                            updateSubscriptionStateUseCase()
                        }
                }

                pushesInitialized = true
            }
        }
    }

    fun hasInitialized(): Boolean {
        return initialized
    }

    fun setLogLevel(logLevel: LogLevel) {
        Logger.logLevel = logLevel
    }
}