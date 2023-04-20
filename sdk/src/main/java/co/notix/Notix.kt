package co.notix

import android.content.Context
import android.content.Intent
import co.notix.audience.NotixAudienceReporter
import co.notix.callback.NotixCallbackHandler
import co.notix.callback.NotixCallbackReporter
import co.notix.data.MainRepository
import co.notix.data.MetricsRepository
import co.notix.data.Storage
import co.notix.di.SingletonComponent
import co.notix.di.contextprovider.ContextProvider
import co.notix.di.contextprovider.ContextProviderInitializer
import co.notix.domain.RequestVars
import co.notix.log.LogLevel
import co.notix.log.Logger
import co.notix.perseverance.worker.SendEventsWorkerLauncher
import co.notix.push.NotificationModifierProviderInitializer
import co.notix.push.NotixNotificationModifier
import co.notix.push.NotixTargetEventHandler
import co.notix.push.TargetEventHandlerProviderInitializer
import co.notix.push.UpdateSubscriptionStateUseCase
import co.notix.push.permissions.NotificationsPermissionActivity
import co.notix.push.permissions.NotificationsPermissionController
import co.notix.services.MetricsService
import co.notix.services.StartupService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

public interface Notix {
    public fun init(
        context: Context,
        notixAppId: String,
        notixToken: String,
        config: NotixSDKConfig = NotixSDKConfig(),
        callbackHandler: NotixCallbackHandler? = null,
    )

    public fun enablePushNotifications(
        context: Context,
        requestVars: RequestVars? = null,
        notificationModifier: NotixNotificationModifier? = null,
        targetEventHandler: NotixTargetEventHandler? = null
    )

    public fun addAudience(audience: String)

    public fun deleteAudience(audience: String)

    public fun setLogLevel(logLevel: LogLevel)

    public companion object : Notix by SingletonComponent.notix
}

internal class NotixImpl(
    private val storage: Storage,
    private val metricsService: MetricsService,
    private val notificationsPermissionController: NotificationsPermissionController,
    private val contextProvider: ContextProvider,
    private val contextProviderInitializer: ContextProviderInitializer,
    private val workerLauncher: SendEventsWorkerLauncher,
    private val mainRepository: MainRepository,
    private val csIo: CoroutineScope,
    private val notixCallbackReporter: NotixCallbackReporter,
    private val updateSubscriptionStateUseCase: UpdateSubscriptionStateUseCase,
    private val metricsRepository: MetricsRepository,
    private val notificationModifierProviderInitializer: NotificationModifierProviderInitializer,
    private val targetEventHandlerProviderInitializer: TargetEventHandlerProviderInitializer,
    private val notixAudienceReporter: NotixAudienceReporter
) : Notix {
    private val initMutex = Mutex()
    private var initialized = false
    private var pushesInitialized = false

    override fun init(
        context: Context,
        notixAppId: String,
        notixToken: String,
        config: NotixSDKConfig,
        callbackHandler: NotixCallbackHandler?,
    ) {
        csIo.launch {
            initMutex.withLock {
                if (initialized) return@launch Logger.e("Notix SDK is already initialized. Exiting init")
                contextProviderInitializer.tryInit(context)

                storage.setAppId(notixAppId)
                storage.setAuthToken(notixToken)
                storage.getUUID()

                if (!config.customUserAgent.isNullOrEmpty()) {
                    storage.setCustomUserAgent(context, config.customUserAgent!!)
                }

                notixCallbackReporter.setHandler(callbackHandler)

                metricsService.onLaunch()

                storage.setInterstitialStartupEnabled(context, config.interstitialStartupEnabled)
                storage.setInterstitialDefaultZoneId(context, config.interstitialDefaultZoneId)
                storage.setInterstitialVars(config.interstitialDefaultVars)

                mainRepository.getConfig(appId = notixAppId, authToken = notixToken)
                    ?: return@launch

                workerLauncher.launch()
                StartupService().interstitialStartup(context = context)

                initialized = true
            }
        }
    }

    override fun enablePushNotifications(
        context: Context,
        requestVars: RequestVars?,
        notificationModifier: NotixNotificationModifier?,
        targetEventHandler: NotixTargetEventHandler?
    ) {
        notificationModifierProviderInitializer.init(notificationModifier)
        targetEventHandlerProviderInitializer.init(targetEventHandler)
        csIo.launch {
            delay(10) // enablePushNotifications is sometimes launched before init. waiting for init to acquire mutex first.
            initMutex.withLock {
                if (!initialized) return@launch Logger.e("Notix SDK was not initialized. Exiting enablePushNotifications")
                if (pushesInitialized) return@launch Logger.e("Push notifications are already initialized. Exiting enablePushNotifications")

                storage.setPushVars(requestVars)

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

    override fun addAudience(audience: String) = notixAudienceReporter.add(audience)

    override fun deleteAudience(audience: String) = notixAudienceReporter.delete(audience)

    fun hasInitialized(): Boolean {
        return initialized
    }

    override fun setLogLevel(logLevel: LogLevel) {
        Logger.logLevel = logLevel
    }
}