package co.notix.data

import co.notix.callback.NotixCallback
import co.notix.callback.NotixCallbackReporter
import co.notix.callback.NotixCallbackStatus
import co.notix.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

internal interface EventRepository {
    suspend fun sendImpression(impressionData: String): Boolean
    suspend fun sendClick(clickData: String): Boolean
}

internal class EventRepositoryImpl(
    private val eventDataSource: EventDataSource,
    private val notixCallbackReporter: NotixCallbackReporter,
    private val cs: CoroutineScope
) : EventRepository {
    override suspend fun sendImpression(impressionData: String) = withContext(cs.coroutineContext) {
        runCatching { eventDataSource.impression(impressionData) }.fold(
            onSuccess = {
                Logger.i(msg = "impression tracked impressionData = $impressionData")
                notixCallbackReporter.report(
                    NotixCallback.Impression(NotixCallbackStatus.SUCCESS, it)
                )
                true
            },
            onFailure = {
                Logger.e(msg = "could not send impression event ${it.message}", throwable = it)
                notixCallbackReporter.report(
                    NotixCallback.Impression(NotixCallbackStatus.FAILURE, it.message)
                )
                false
            }
        )
    }

    override suspend fun sendClick(clickData: String) = withContext(cs.coroutineContext) {
        runCatching { eventDataSource.click(clickData) }.fold(
            onSuccess = {
                Logger.i("click tracked clickData = $clickData")
                notixCallbackReporter.report(
                    NotixCallback.Click(NotixCallbackStatus.SUCCESS, it)
                )
                true
            },
            onFailure = {
                Logger.e(msg = "could not send click event ${it.message}", throwable = it)
                notixCallbackReporter.report(
                    NotixCallback.Click(NotixCallbackStatus.FAILURE, it.message)
                )
                false
            }
        )
    }
}