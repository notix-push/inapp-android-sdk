package com.notix.notixsdk.interstitial.data

import com.notix.notixsdk.domain.DomainModels
import com.notix.notixsdk.log.Logger
import com.notix.notixsdk.providers.StorageProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

interface InterstitialRepository {
    suspend fun getInterstitial(
        adType: AdType,
        zoneId: Long? = null,
        vars: DomainModels.RequestVars? = null,
        experiment: Int? = null,
        onLoadCallback: () -> Unit,
        onLoadFailedCallback: (() -> Unit)? = null,
    )
}

class InterstitialRepositoryImpl(
    private val storage: StorageProvider,
    private val interstitialDataSource: InterstitialDataSource,
    private val cs: CoroutineScope
) : InterstitialRepository {
    override suspend fun getInterstitial(
        adType: AdType,
        zoneId: Long?,
        vars: DomainModels.RequestVars?,
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