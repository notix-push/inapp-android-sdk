package co.notix.push.data

import co.notix.R
import co.notix.callback.NotixCallback
import co.notix.callback.NotixCallbackReporter
import co.notix.callback.NotixCallbackStatus
import co.notix.data.Storage
import co.notix.di.contextprovider.ContextProvider
import co.notix.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

internal interface PushRepository {
    suspend fun getPushData(pingData: String): Result<PushDataResponse?>
    suspend fun subscribe(token: String)
    suspend fun unsubscribe()
}

internal class PushRepositoryImpl(
    private val pushDataSource: PushDataSource,
    private val storage: Storage,
    private val contextProvider: ContextProvider,
    private val cs: CoroutineScope,
    private val notixCallbackReporter: NotixCallbackReporter
) : PushRepository {
    override suspend fun getPushData(pingData: String) = withContext(cs.coroutineContext) {
        runCatching {
            pushDataSource.getPushData(pingData)
        }.onSuccess {
            notixCallbackReporter.report(
                NotixCallback.PushDataLoad(NotixCallbackStatus.SUCCESS, it.toString())
            )
        }.onFailure {
            Logger.e(
                msg = "couldn't get push data for pingData = $pingData",
                throwable = it
            )
            notixCallbackReporter.report(
                NotixCallback.PushDataLoad(NotixCallbackStatus.FAILURE, it.message)
            )
        }
    }

    override suspend fun subscribe(token: String) = withContext(cs.coroutineContext) {
        runCatching {
            val appId = storage.getAppId() ?: error("subscribe - invalid data. appId = null")
            val uuid = storage.getUUID()
            val packageName = contextProvider.appContext.packageName
            val createdDate = storage.getCreatedDate()
            val sdkVersion = contextProvider.appContext.getString(R.string.sdk_version)
            val vars = storage.getPushVars()

            pushDataSource.subscribe(
                appId = appId,
                uuid = uuid,
                packageName = packageName,
                token = token,
                createdDate = createdDate,
                sdkVersion = sdkVersion,
                vars = vars
            )
        }.onSuccess {
            Logger.i(msg = "successfully subscribed")
            notixCallbackReporter.report(
                NotixCallback.Subscription(NotixCallbackStatus.SUCCESS, it)
            )
        }.onFailure {
            Logger.e(msg = "subscription fail", throwable = it)
            notixCallbackReporter.report(
                NotixCallback.Subscription(NotixCallbackStatus.FAILURE, it.message)
            )
        }.fold(
            onSuccess = {
                storage.setFcmToken(token)
                storage.setSubscriptionActive(true)
            },
            onFailure = {}
        )
    }

    override suspend fun unsubscribe() = withContext(cs.coroutineContext) {
        runCatching {
            val appId = storage.getAppId() ?: error("unsubscribe - invalid data. appId = null")
            val uuid = storage.getUUID()
            val packageName = contextProvider.appContext.packageName

            pushDataSource.unsubscribe(appId = appId, uuid = uuid, packageName = packageName)
        }.onSuccess {
            notixCallbackReporter.report(
                NotixCallback.Unsubscription(NotixCallbackStatus.SUCCESS, it)
            )
        }.onFailure {
            notixCallbackReporter.report(
                NotixCallback.Unsubscription(NotixCallbackStatus.FAILURE, it.message)
            )
        }.fold(
            onSuccess = {
                Logger.i(msg = "successfully unsubscribed")
                storage.removeFcmToken()
                storage.setSubscriptionActive(false)
            },
            onFailure = {
                Logger.e(msg = "unsubscription fail", throwable = it)
            }
        )
    }
}