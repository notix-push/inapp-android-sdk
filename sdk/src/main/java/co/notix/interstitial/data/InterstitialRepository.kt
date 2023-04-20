package co.notix.interstitial.data

import co.notix.domain.RequestVars
import co.notix.log.Logger
import co.notix.data.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

internal interface InterstitialRepository {
    suspend fun getInterstitial(
        adType: AdType,
        zoneId: Long? = null,
        vars: RequestVars? = null,
        experiment: Int? = null,
        onLoadCallback: () -> Unit,
        onLoadFailedCallback: (() -> Unit)? = null,
    )
}

internal class InterstitialRepositoryImpl(
    private val storage: Storage,
    private val interstitialDataSource: InterstitialDataSource,
    private val cs: CoroutineScope
) : InterstitialRepository {
    override suspend fun getInterstitial(
        adType: AdType,
        zoneId: Long?,
        vars: RequestVars?,
        experiment: Int?,
        onLoadCallback: () -> Unit,
        onLoadFailedCallback: (() -> Unit)?,
    ): Unit = withContext(cs.coroutineContext) {
        val uuid = storage.getUUID()
        val createdDate = storage.getCreatedDate()
        val appId = storage.getAppId() ?: error("getInterstitial - invalid data. appId null")
        val pubId = storage.getPubId() ?: error("getInterstitial - invalid data. pubId null")
        runCatching {
            interstitialDataSource.getInterstitial(
                uuid = uuid,
                appId = appId,
                pubId = pubId,
                createdDate = createdDate,
                zoneId = zoneId,
                vars = vars,
                experiment = experiment
            )
        }.fold(
            onSuccess = {
                Logger.i("interstitial loaded")
                when (adType) {
                    AdType.INTERSTITIAL -> storage.setInterstitialPayload(it)
                    AdType.CONTENT -> storage.setMessageContentPayload(it)
                }
                onLoadCallback()
            },
            onFailure = {
                Logger.e("interstitial loading failed. ${it.message}")
                onLoadFailedCallback?.invoke()
            }
        )
    }
}