package com.notix.notixsdk.data

import com.notix.notixsdk.NotixCallbackReporter
import com.notix.notixsdk.domain.NotixCallback
import com.notix.notixsdk.domain.NotixCallbackStatus
import com.notix.notixsdk.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

interface EventRepository {
    suspend fun sendImpression(impressionData: String): Boolean
    suspend fun sendClick(clickData: String): Boolean
}

class EventRepositoryImpl(
    private val eventDataSource: EventDataSource,
    private val notixCallbackReporter: NotixCallbackReporter,
    private val cs: CoroutineScope
) : EventRepository {
    override suspend fun sendImpression(impressionData: String) = withContext(cs.coroutineContext) {
        runCatching { eventDataSource.impression(impressionData) }.fold(
            onSuccess = {
                Logger.i(msg = "impression tracked")
                notixCallbackReporter.report(
                    NotixCallback.Impression(NotixCallbackStatus.SUCCESS, it)
                )
                true
            },
            onFailure = {
                Logger.e(msg = "could not send impression event ${it.message}", throwable = it)
                notixCallbackReporter.report(
                    NotixCallback.Impression(NotixCallbackStatus.FAILED, it.message)
                )
                false
            }
        )
    }

    override suspend fun sendClick(clickData: String) = withContext(cs.coroutineContext) {
        runCatching { eventDataSource.click(clickData) }.fold(
            onSuccess = {
                Logger.i("click tracked")
                notixCallbackReporter.report(
                    NotixCallback.Click(NotixCallbackStatus.SUCCESS, it)
                )
                true
            },
            onFailure = {
                Logger.e(msg = "could not send click event ${it.message}", throwable = it)
                notixCallbackReporter.report(
                    NotixCallback.Click(NotixCallbackStatus.FAILED, it.message)
                )
                false
            }
        )
    }
}