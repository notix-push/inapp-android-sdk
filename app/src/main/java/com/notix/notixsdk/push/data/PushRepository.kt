package com.notix.notixsdk.push.data

import com.notix.notixsdk.NotixCallbackReporter
import com.notix.notixsdk.R
import com.notix.notixsdk.di.contextprovider.ContextProvider
import com.notix.notixsdk.domain.NotixCallback
import com.notix.notixsdk.domain.NotixCallbackStatus
import com.notix.notixsdk.log.Logger
import com.notix.notixsdk.providers.StorageProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

interface PushRepository {
    suspend fun subscribe(token: String)
    suspend fun unsubscribe()
}

class PushRepositoryImpl(
    private val pushDataSource: PushDataSource,
    private val storage: StorageProvider,
    private val contextProvider: ContextProvider,
    private val cs: CoroutineScope,
    private val notixCallbackReporter: NotixCallbackReporter
) : PushRepository {
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
                NotixCallback.Subscription(NotixCallbackStatus.FAILED, it.message)
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
                NotixCallback.Unsubscription(NotixCallbackStatus.FAILED, it.message)
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