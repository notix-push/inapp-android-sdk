package co.notix.data

import co.notix.callback.NotixCallbackReporter
import co.notix.di.contextprovider.ContextProvider
import co.notix.callback.NotixCallback
import co.notix.callback.NotixCallbackStatus
import co.notix.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

internal interface MainRepository {
    suspend fun getConfig(appId: String, authToken: String): RequestModels.ConfigModel?
    suspend fun manageAudience(action: String, audience: String)
}

internal class MainRepositoryImpl(
    private val mainDataSource: MainDataSource,
    private val storage: Storage,
    private val contextProvider: ContextProvider,
    private val cs: CoroutineScope,
    private val notixCallbackReporter: NotixCallbackReporter
) : MainRepository {
    override suspend fun getConfig(appId: String, authToken: String) =
        runCatching { mainDataSource.getConfig(appId, authToken) }
            .mapCatching { if (it.appId == "" || it.pubId == 0) error("invalid config $it") else it }
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
                        NotixCallback.ConfigLoaded(NotixCallbackStatus.FAILURE, it.message)
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
                    NotixCallback.ManageAudience(NotixCallbackStatus.FAILURE, it.message)
                )
            }
        }
}