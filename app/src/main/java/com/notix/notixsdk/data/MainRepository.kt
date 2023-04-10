package com.notix.notixsdk.data

import com.notix.notixsdk.NotixCallbackReporter
import com.notix.notixsdk.di.contextprovider.ContextProvider
import com.notix.notixsdk.domain.NotixCallback
import com.notix.notixsdk.domain.NotixCallbackStatus
import com.notix.notixsdk.log.Logger
import com.notix.notixsdk.providers.StorageProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

interface MainRepository {
    suspend fun getConfig(appId: String, authToken: String): RequestModels.ConfigModel?
    suspend fun manageAudience(action: String, audience: String)
}

class MainRepositoryImpl(
    private val mainDataSource: MainDataSource,
    private val storage: StorageProvider,
    private val contextProvider: ContextProvider,
    private val cs: CoroutineScope,
    private val notixCallbackReporter: NotixCallbackReporter
) : MainRepository {
    override suspend fun getConfig(appId: String, authToken: String) =
        runCatching { mainDataSource.getConfig(appId, authToken) }
            .mapCatching { if (it.appId == "" || it.senderId == 0L || it.pubId == 0) error("invalid config $it") else it }
            .fold(
                onSuccess = {
                    notixCallbackReporter.report(
                        NotixCallback.ConfigLoaded(NotixCallbackStatus.SUCCESS, it.toString())
                    )
                    storage.setSenderId(it.senderId)
                    storage.setPubId(it.pubId)
                    storage.setAppId(it.appId)
                    storage.setAuthToken(authToken)
                    storage.getUUID()
                    it
                },
                onFailure = {
                    Logger.e(msg = "error while getting config ${it.message}", throwable = it)
                    notixCallbackReporter.report(
                        NotixCallback.ConfigLoaded(NotixCallbackStatus.FAILED, it.message)
                    )
                    null
                }
            )

    override suspend fun manageAudience(action: String, audience: String): Unit =
        withContext(cs.coroutineContext) {
            runCatching {
                if (audience.isEmpty()) error("manageAudience: audience is empty")
                val authToken =
                    storage.getAuthToken() ?: error("manageAudience: authToken is empty")
                val appId = storage.getAppId() ?: error("manageAudience: app id is empty")
                val uuid = storage.getUUID()
                val pubId = storage.getPubId() ?: error("manageAudience: pub id")
                val packageName = contextProvider.appContext.packageName
                mainDataSource.manageAudience(
                    authToken = authToken,
                    action = action,
                    audience = audience,
                    uuid = uuid,
                    appId = appId,
                    pubId = pubId,
                    packageName = packageName
                )
            }.onSuccess {
                Logger.i("audience managed")
                notixCallbackReporter.report(
                    NotixCallback.ManageAudience(NotixCallbackStatus.SUCCESS, it)
                )
            }.onFailure {
                Logger.e(msg = "couldn't manage audience ${it.message}", throwable = it)
                notixCallbackReporter.report(
                    NotixCallback.ManageAudience(NotixCallbackStatus.FAILED, it.message)
                )
            }
        }
}